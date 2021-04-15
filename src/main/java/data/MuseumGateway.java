package data;

import application.LocationStrategy;
import application.ScoreStrategy;
import application.SearchStrategy;
import io.ebean.DB;
import io.ebean.SqlRow;
import model.Museum;
import model.query.QMuseum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuseumGateway {
    private static final Logger logger = LoggerFactory.getLogger(MuseumGateway.class);
    private static MuseumGateway instance = null;
    private SearchStrategy searchStrategy = new LocationStrategy();


    public static MuseumGateway getInstance() {
        logger.info("Retrieving MuseumGateway instance...");
        if (instance == null) {
            logger.info("MuseumGateway first initialization...");
            instance = new MuseumGateway();
        }
        logger.info("MuseumGateway instance retrieved");
        return instance;
    }

    public String searchMuseums(String query, String location) {
        String[] keywords = splitQuery(query);
        String sql = searchStrategy.buildSelect(keywords, location);
        SqlRow row = DB.sqlQuery(sql)
                .findOne();
        if (row != null) {
            return row.getString("json");
        } else throw new IllegalArgumentException();
    }

    public Museum getMuseum(long museumId) {
        return new QMuseum()
                .museumId.eq(museumId)
                .findOne();
    }

    private String[] splitQuery(String query) {
        logger.info("Splitting query \"" + query + "\" into keywords...");

        String[] keywords;
        String sql = "SELECT array_to_string(tsvector_to_array(to_tsvector('simple_italian', ?)), '|');";
        SqlRow row = DB.sqlQuery(sql)
                .setParameter(1, query)
                .findOne();
        if (row != null) {
            keywords = row.getString("array_to_string").split("\\|");
        } else throw new IllegalArgumentException();
        logger.info("Query \"" + query + "\" has been split into keywords (" + String.join(", ", keywords) + ")");
        return keywords;
    }
}
