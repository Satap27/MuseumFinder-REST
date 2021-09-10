package data;

import business_logic.LocationStrategy;
import business_logic.ScoreStrategy;
import business_logic.SearchStrategy;
import io.ebean.DB;
import io.ebean.DocumentStore;
import io.ebean.PagedList;
import io.ebean.SqlRow;
import io.ebean.search.MultiMatch;
import model.Museum;
import model.query.QCategory;
import model.query.QMuseum;
import model.query.QUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Nullable;

import java.util.ArrayList;
import java.util.List;

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

    static double haversine(Double lat1, Double lon1,
                            Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null) {
            return 10000;
        }
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
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
                .categories.fetch(QCategory.alias().name)
                .owners.fetch(QUser.alias().username)
                .findOne();
    }

    public long saveMuseum(Museum museum) {
        DB.save(museum);
        return museum.getMuseumId();
    }

    public void initializeIndex() {
        DocumentStore documentStore = DB.getDefault().docStore();
        documentStore.indexAll(Museum.class);
    }

    public List<Museum> fullText(String query) {
        MultiMatch multiMatch =
                MultiMatch.fields("description", "name")
                        .opAnd()
                        .type(MultiMatch.Type.CROSS_FIELDS);

        //CROSS_FIELDS Treats fields with the same analyzer as though they were one big field. Looks for each word in any field.

        PagedList<Museum> museums = DB.find(Museum.class)
                .text()
                .multiMatch(query, multiMatch)
                .select("museumId, name")
                .setMaxRows(100)
                .findPagedList();
        List<Museum> museumList = museums.getList();
        if (searchStrategy.getClass() == LocationStrategy.class) {
            //museumList.removeIf(museum -> haversine(museum.getLat(), museum.getLng(), 10., 40.) >= 50);
            List<Museum> museumsFiltered = new ArrayList<>();
            for (Museum museum: museumList) {
                if (haversine(museum.getLat(), museum.getLng(), 10., 43.) <= 50)
                    museumsFiltered.add(museum);
            }
            return museumsFiltered;
        }
        return museumList;
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
