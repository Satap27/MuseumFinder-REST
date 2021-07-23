package business_logic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import model.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

    @Override
    public String buildSelect(String[] keywords, String location) {
        logger.info("Building SQL query based on location...");
        String lat, lng;
        logger.info("Retrieving location coordinates with OpenStreetMap API...");
        try (InputStream inputStream = new URL("https://nominatim.openstreetmap.org/search/?q=" + location + "&countrycodes=it&format=json&limit=1").openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String jsonText = readAll(reader);
            if (jsonText.equals("[]")) {
                logger.warn("Cannot retrieve coordinates, falling back to score strategy");
                return new ScoreStrategy().buildSelect(keywords, null);
            }
            JsonArray jsonArray = new Gson().fromJson(jsonText, JsonArray.class);
            lat = jsonArray.get(0).getAsJsonObject().get("lat").getAsString();
            lng = jsonArray.get(0).getAsJsonObject().get("lon").getAsString();
        } catch (JsonSyntaxException | IOException | NullPointerException e) {
            logger.error(Log.getStringStackTrace(e));
            logger.warn("Falling back to score strategy");
            return new ScoreStrategy().buildSelect(keywords, null);
            // TODO RAISE?
        }
        logger.info("Coordinates retrieved (" + lat + ", " + lng + ")");
        String query = "SELECT to_jsonb(array_agg(list)) AS json FROM (SELECT name, museum_id, lat, lng FROM (SELECT (";
        for (String keyword : keywords) {
            query = query.concat(String.format("ts_rank_cd(m.description_tsv, to_tsquery('italian', '%s'))/(1 + " +
                            "(SELECT sum(ts_rank_cd(m.description_tsv, to_tsquery('italian', '%s'))) FROM museum as m)) + ",
                    keyword, keyword));
        }
        query = query.substring(0, query.length() - 3).concat(") AS score, m.* FROM (SELECT * FROM museum " +
                "WHERE haversine(" + lat + ", " + lng + ", lat, lng) < 100) m) S WHERE score > 0 ORDER BY score DESC LIMIT 100) list;");
        logger.debug("QUERY:" + query);
        return query;
    }
}
