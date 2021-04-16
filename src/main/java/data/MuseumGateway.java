package data;

import application.ScoreStrategy;
import application.SearchStrategy;
import io.ebean.DB;
import io.ebean.SqlRow;
import model.Museum;
import model.query.QMuseum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Nullable;

public class MuseumGateway {
    private static final Logger logger = LoggerFactory.getLogger(MuseumGateway.class);
    private static MuseumGateway instance = null;
    private SearchStrategy searchStrategy = new ScoreStrategy();


    /**
     * Returns the only MuseumGateway instance (Singleton pattern)
     *
     * @return the MuseumGateway instance
     */
    public static MuseumGateway getInstance() {
        logger.info("Retrieving MuseumGateway instance...");
        if (instance == null) {
            logger.info("MuseumGateway first initialization...");
            instance = new MuseumGateway();
        }
        logger.info("MuseumGateway instance retrieved");
        return instance;
    }

    /**
     * Sets a new search strategy (<b>ScoreStrategy</b> or <b>LocationStrategy</b>).
     *
     * @param searchStrategy a searchStrategy implementation
     */
    public void setStrategy(SearchStrategy searchStrategy) {
        logger.info("Setting new search strategy (" + searchStrategy.getClass().getName() + ")...");
        this.searchStrategy = searchStrategy;
        logger.info(searchStrategy.getClass().getName() + " set");
    }

    /**
     * Returns the list of museums found with the full-text search, given a query and an optional location.
     *
     * @param query    a string query for the full-text search
     * @param location a nullable string location
     * @return a JSON string with a list of museums
     */
    public String searchMuseums(String query, @Nullable String location) {
        String[] keywords = splitQuery(query);
        String sql = searchStrategy.buildSelect(keywords, location);
        SqlRow row = DB.sqlQuery(sql)
                .findOne();
        if (row != null) {
            return row.getString("json");
        } else throw new IllegalArgumentException();
    }

    /**
     * Returns a Museum object if the museum with the given id exists in the database, or <b>null</b> otherwise.
     *
     * @param museumId the museum id
     * @return a Museum object
     */
    public Museum getMuseum(long museumId) {
        return new QMuseum()
                .museumId.eq(museumId)
                .findOne();
    }

    /**
     * Returns the list of keywords, generated from a query on an italian PostgreSQL dictionary.
     *
     * @param query a string query
     * @return the list of keywords retrieved from the query
     */
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
