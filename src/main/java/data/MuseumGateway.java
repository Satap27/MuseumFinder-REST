package data;

import business_logic.ScoreStrategy;
import business_logic.SearchStrategy;
import io.ebean.DB;
import io.ebean.DocumentStore;
import io.ebean.PagedList;
import io.ebean.search.MultiMatch;
import model.Museum;
import model.query.QCategory;
import model.query.QMuseum;
import model.query.QUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Nullable;

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

    public List<Museum> searchMuseums(String query, @Nullable String location) {
        MultiMatch multiMatch =
                MultiMatch.fields("description", "name")
                        .opAnd()
                        .type(MultiMatch.Type.CROSS_FIELDS);


        PagedList<Museum> museums = DB.find(Museum.class)
                .text()
                .multiMatch(query, multiMatch)
                .select("museumId, name, lat, lng")
                .setMaxRows(100)
                .findPagedList();
        return searchStrategy.filterList(museums.getList(), location);
    }
}
