package business_logic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.ebean.DB;
import model.Log;
import model.Museum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LocationStrategy implements SearchStrategy {
    private static final Logger logger = LoggerFactory.getLogger(LocationStrategy.class);

    private static String readAll(Reader reader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            stringBuilder.append((char) c);
        }
        return stringBuilder.toString();
    }

    static double haversine(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null) {
            return 10000;
        }
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }

    @Override
    public List<Museum> filterList(List<Museum> museums, @Nullable String location) {
        Double lat, lng;
        List<Museum> museumList = new ArrayList<>();
        logger.info("Retrieving location coordinates with OpenStreetMap API...");
        try (InputStream inputStream = new URL("https://nominatim.openstreetmap.org/search/?q=" + location + "&countrycodes=it&format=json&limit=1").openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String jsonText = readAll(reader);
            if (jsonText.equals("[]")) {
                logger.warn("Cannot retrieve coordinates, falling back to score strategy");
                return new ScoreStrategy().filterList(museums, null);
            }
            JsonArray jsonArray = new Gson().fromJson(jsonText, JsonArray.class);
            lat = jsonArray.get(0).getAsJsonObject().get("lat").getAsDouble();
            lng = jsonArray.get(0).getAsJsonObject().get("lon").getAsDouble();
        } catch (JsonSyntaxException | IOException | NullPointerException e) {
            logger.error(Log.getStringStackTrace(e));
            logger.warn("Falling back to score strategy");
            return new ScoreStrategy().filterList(museums, null);
        }
        logger.info("Coordinates retrieved (" + lat + ", " + lng + ")");
        String museumsJsonArray = DB.json().toJson(museums);
        Type museumListType = new TypeToken<ArrayList<Museum>>(){}.getType();
        museums = new Gson().fromJson(museumsJsonArray, museumListType);
        for (Museum museum : museums) {
            try {
                if (haversine(museum.getLat(), museum.getLng(), lat, lng) <= 100)
                    museumList.add(museum);
            }
            catch (Exception ignored) { }
        }
        return museumList;
    }
}
