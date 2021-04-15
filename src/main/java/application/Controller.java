package application;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import data.MuseumGateway;
import io.ebean.DB;
import model.Log;
import model.Museum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Controller {
    static final Logger logger = LoggerFactory.getLogger(Controller.class);
    MuseumGateway museumGateway = MuseumGateway.getInstance();

    /**
     * Returns a JSON encoded museum, based on the provided museum id.
     *
     * @param   museumId    a string which should contain a museum id (long)
     * @return              Museum object as a JSON string
     */
    public String getMuseum(String museumId) {
        long parsedId;
        try {
            parsedId = Long.parseLong(museumId);
        } catch (NumberFormatException e) {
            logger.error(Log.getStringStackTrace(e));
            // TODO
            return "QUERY MALFORMATA";
        }
        Museum museum;
        try {
            museum = museumGateway.getMuseum(parsedId);
        }
        catch (Exception e) {
            logger.error(Log.getStringStackTrace(e));
            // TODO
            return "ECCEZIONE";
        }
        if (museum == null) {
            // TODO
            logger.warn("Museum with id " + museumId + " not found!");
            return "MUSEO NON TROVATO";
        }
        else
            return DB.json().toJson(museum);
    }

    /**
     * Returns a JSON encoded list of museums, based on the query and the location
     * provided by the user in the request body.
     *
     * @param   body    a JSON string which represents the request body.
     *                  The body must contain <b>query</b> and <b>location</b> properties
     * @return          response body as a JSON string
     */
    public String searchMuseums(String body) {
        String query, location;
        try {
            Map<String, String> json = new Gson().fromJson(body, Map.class);
            query = json.get("query");
            location = json.get("location");
            museumGateway.setStrategy(location != null ? new LocationStrategy() : new ScoreStrategy());
        } catch (JsonSyntaxException e) {
            logger.error(Log.getStringStackTrace(e));
            // TODO
            return "MALFORMATA";
        }
        String responseBody = museumGateway.searchMuseums(query, location);
        if (responseBody == null) {
            logger.warn("No results for query '" + query + "' and location '" + location + "'");
            // TODO
            return "NIENTE";
        }
        return responseBody;
    }
}
