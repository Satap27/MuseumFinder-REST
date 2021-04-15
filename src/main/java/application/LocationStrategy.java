package application;

import com.google.gson.Gson;
import model.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
        // TODO probabilmente non deve funzionare così (deve filtrare i musei troppo distanti e poi andare per score)
        logger.info("Building SQL query based on location...");
        String lat, lng, jsonText = "[]";
            logger.info("Retrieving location coordinates with OpenStreetMap API...");
        try (InputStream inputStream = new URL("https://nominatim.openstreetmap.org/search/?q=" + location + "&countrycodes=it&format=json&limit=1").openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            jsonText = readAll(reader);
        } catch (IOException e) {
            logger.error(Log.getStringStackTrace(e));
            // TODO RAISE?
        }
        // TODO CONTROLLI
        if (jsonText.equals("[]")) {
            logger.warn("Cannot retrieve coordinates, falling back to score strategy");
            return new ScoreStrategy().buildSelect(keywords, null);
        }
        List<Map<String,Object>> jsonArray = new Gson().fromJson(jsonText, List.class);
        lat = (String) jsonArray.get(0).get("lat");
        lng = (String) jsonArray.get(0).get("lon");
        logger.info("Coordinates retrieved (" + lat + ", " + lng + ")");
        String query = "SELECT to_jsonb(array_agg(list)) AS json FROM(SELECT * FROM (SELECT name, museum_id, lat, lng FROM (SELECT (";
        for (String keyword : keywords) {
            query = query.concat(String.format("ts_rank_cd(P.description_tsv, to_tsquery('italian', '%s'))/(1 + " +
                            "(SELECT sum(ts_rank_cd(P.description_tsv, to_tsquery('italian', '%s'))) FROM museum as P)) + ",
                    keyword, keyword));
        }
        query = query.substring(0, query.length() - 3).concat(") AS score, P.name, P.museum_id, P.lat, P.lng FROM museum as P WHERE lat IS NOT NULL and lng IS NOT NULL) S WHERE score > 0 " +
                "ORDER BY score DESC LIMIT 50) list order by haversine(" + lat + ", " + lng + ", lat, lng)) list;");
        logger.debug("QUERY:" + query);
        return query;
    }
}
