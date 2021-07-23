package business_logic;

import com.google.gson.*;
import data.MuseumGateway;
import io.ebean.DB;
import model.Log;
import model.Museum;
import model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private final MuseumGateway museumGateway = MuseumGateway.getInstance();
    private final Gson gson = new Gson();

    /**
     * Returns a JSON encoded museum, based on the provided museum id.
     *
     * @param museumId a string which should contain a museum id (long)
     * @return Museum object as a JSON string
     */
    public String getMuseum(String museumId) {
        long parsedId;
        Response response = new Response();
        try {
            parsedId = Long.parseLong(museumId);
        } catch (NumberFormatException e) {
            // TODO 422 (unprocessable entity)
            logger.error(Log.getStringStackTrace(e));
            response.setMessage("Provided museum id is not a number!");
            return gson.toJson(response);
        }
        Museum museum;
        try {
            museum = museumGateway.getMuseum(parsedId);
        } catch (Exception e) {
            // TODO 500 (internal server error)
            logger.error(Log.getStringStackTrace(e));
            response.setMessage("Something went wrong!");
            return gson.toJson(response);
        }
        if (museum == null) {
            // TODO 404 (not found)
            logger.warn("Museum with id " + museumId + " not found!");
            response.setMessage("Museum not found.");
            return gson.toJson(response);
        } else {
            return gson.toJson(encapsulateJsonObject(gson.toJsonTree(response), "museum", DB.json().toJson(museum)));
        }
    }

    /**
     * Returns a JSON encoded list of museums, based on the query and the location
     * provided by the user in the request body.
     *
     * @param body a JSON string which represents the request body.
     *             The body must contain <b>query</b> and <b>location</b> properties
     * @return response body as a JSON string
     */
    public String searchMuseums(String body) {
        String query, location;
        Response response = new Response();
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            JsonElement queryElement = json.get("query");
            JsonElement locationElement = json.get("location");
            if (queryElement.isJsonNull())
                throw new IllegalArgumentException("a query value is required and can't be null");
            query = queryElement.getAsString();
            location = locationElement.isJsonNull() ? null : locationElement.getAsString();
            museumGateway.setStrategy((location != null && !location.equals("")) ? new LocationStrategy() : new ScoreStrategy());
        } catch (JsonSyntaxException | IllegalArgumentException | NullPointerException e) {
            // TODO 422 (unprocessable entity)
            logger.error(Log.getStringStackTrace(e));
            response.setMessage("Malformed query!");
            return gson.toJson(response);
        }
        String museumsJsonArray = museumGateway.searchMuseums(query, location);
        if (museumsJsonArray == null) {
            logger.warn("No results for query '" + query + "' and location '" + location + "'");
            response.setMessage("No results");
            return gson.toJson(encapsulateJsonArray(gson.toJsonTree(response), "museums", "[]"));
        }
        return gson.toJson(encapsulateJsonArray(gson.toJsonTree(response), "museums", museumsJsonArray));
    }

    private JsonElement encapsulateJsonObject(JsonElement jsonElement, String name, String json) {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        jsonElement.getAsJsonObject().add(name, gson.toJsonTree(jsonObject));
        return jsonElement;
    }

    private JsonElement encapsulateJsonArray(JsonElement jsonElement, String name, String json) {
        JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
        jsonElement.getAsJsonObject().add(name, gson.toJsonTree(jsonArray));
        return jsonElement;
    }
}
