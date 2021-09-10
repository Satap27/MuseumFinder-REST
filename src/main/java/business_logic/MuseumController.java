package business_logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.ebean.DB;
import model.Log;
import model.Message;
import model.Museum;
import model.User;
import org.sonatype.inject.Nullable;
import spark.Response;

import java.util.Objects;

public class MuseumController extends Controller{

    /**
     * Returns a JSON encoded museum, based on the provided museum id.
     *
     * @param museumId a string which should contain a museum id (long)
     * @param response the HTTPS Spark response
     * @return the Spark response with a Museum object as the JSON body
     */
    public Response getMuseumResponse(String museumId, Response response) {
        Message message = new Message();
        Museum museum;
        try {
            museum = getMuseum(museumId);
        } catch (ResponseException e) {
            return generateErrorResponse(response, e.getMessage(), e.getStatusCode());
        }
        response.body(gson.toJson(encapsulateJsonObject(gson.toJsonTree(message), "museum", DB.json().toJson(museum))));
        return response;
    }

    public Museum getMuseum(String museumId) throws ResponseException {
        long parsedId;
        try {
            parsedId = Long.parseLong(museumId);
        } catch (NumberFormatException e) {
            logger.error(Log.getStringStackTrace(e));
            throw new ResponseException("Provided museum id is not a number!", 422);
        }
        Museum museum;
        try {
            museum = museumGateway.getMuseum(parsedId);
        } catch (Exception e) {
            logger.error(Log.getStringStackTrace(e));
            throw new ResponseException("Something went wrong!", 500);
        }
        if (museum == null) {
            logger.warn("Museum with id " + museumId + " not found!");
            throw new ResponseException("Museum not found.", 404);
        }
        return museum;
    }

    /**
     * Creates a new museum in the database or update the status of an existing one,
     * based on the info provided by the user in the request body.
     *
     * @param body     a JSON string which represents the request body.
     *                 The body must contain a not null <b>name</b> property
     * @param response the HTTPS Spark response
     * @return the Spark response
     */
    public Response saveMuseumResponse(String body, Response response, @Nullable String museumId, @Nullable String token) {
        Museum museum = null;
        long id;
        try {
            if (museumId != null)
                museum = getMuseum(museumId);
            checkPrivilegesOverMuseum(museum, token);
            id = modifyMuseum(museum, body);
        } catch (ResponseException e) {
            return generateErrorResponse(response, e.getMessage(), e.getStatusCode());
        }
        return getMuseumResponse(Long.toString(id), response);
    }

    private long modifyMuseum(Museum museum, String body) throws ResponseException {
        Museum newMuseum = gson.fromJson(body, Museum.class);
        if (museum == null)
            museum = newMuseum;
        else
            museum.updateWith(newMuseum);
        if (museum.getName() == null || Objects.equals(museum.getName(), "")) {
            throw new ResponseException("Museum name can't be null or empty!", 422);
        }
        return saveMuseum(museum);
    }

    private long saveMuseum(Museum museum) throws ResponseException {
        long id;
        try {
            id = museumGateway.saveMuseum(museum);
        } catch (Exception e) {
            logger.error(Log.getStringStackTrace(e));
            throw new ResponseException("Something went wrong!", 500);
        }
        return id;
    }

    private void checkPrivilegesOverMuseum(Museum museum, String clientToken) throws ResponseException {
        User client;
        try {
            client = decryptJTW(clientToken);
        } catch (Exception e) {
            logger.error(Log.getStringStackTrace(e));
            throw new ResponseException("Unauthorized", 401);
        }
        if (client.getRole() == 2 && (museum == null || !client.getOwnedMuseums().contains(museum))) {
            throw new ResponseException("Forbidden", 403);
        }
    }

    /**
     * Returns a JSON encoded list of museums, based on the query and the location
     * provided by the user in the request body.
     *
     * @param body     a JSON string which represents the request body.
     *                 The body must contain <b>query</b> and <b>location</b> properties
     * @param response the HTTPS Spark response
     * @return the Spark response with a museum list as the JSON body
     */
    public Response searchMuseums(String body, Response response) {
        String query, location;
        Message message = new Message();
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
            logger.error(Log.getStringStackTrace(e));
            return generateErrorResponse(response, "Malformed query!", 422);
        }
        String museumsJsonArray = DB.json().toJson(museumGateway.fullText(query));
        if (Objects.equals(museumsJsonArray, "[]")) {
            logger.warn("No results for query '" + query + "' and location '" + location + "'");
            message.setMessage("No results");
        }
        response.body(gson.toJson(encapsulateJsonArray(gson.toJsonTree(message), "museums", museumsJsonArray)));
        return response;
    }

    public Response addOwner(String museumId, String ownerId, Response response, String token) {
        Museum museum;
        User owner;
        try {
            checkIfAdmin(token);
            museum = getMuseum(museumId);
            owner = getUser(ownerId);
            museum.getOwners().add(owner);
            saveMuseum(museum);
        } catch (ResponseException e) {
            return generateErrorResponse(response, e.getMessage(), e.getStatusCode());
        }
        return getMuseumResponse(museumId, response);
    }

    private void checkIfAdmin(String clientToken) throws ResponseException {
        User client;
        try {
            client = decryptJTW(clientToken);
        } catch (Exception e) {
            logger.error(Log.getStringStackTrace(e));
            throw new ResponseException("Unauthorized", 401);
        }
        if (client.getRole() != 1) {
            throw new ResponseException("Forbidden", 403);
        }
    }

}
