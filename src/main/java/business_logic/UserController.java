package business_logic;

import io.ebean.DB;
import model.Log;
import model.Message;
import model.User;
import org.sonatype.inject.Nullable;
import spark.Response;

import java.util.Objects;

public class UserController extends Controller {

    public Response getUserResponse(String userId, Response response) {
        Message message = new Message();
        User user;
        try {
            user = getUser(userId);
        } catch (ResponseException e) {
            return generateErrorResponse(response, e.getMessage(), e.getStatusCode());
        }
        response.body(gson.toJson(encapsulateJsonObject(gson.toJsonTree(message), "user", DB.json().toJson(user))));
        return response;
    }

    public Response saveUserResponse(String body, Response response, @Nullable String userId, String token) {
        User user = null;
        long id;
        try {
            if (userId != null)
                user = getUser(userId);
            checkPrivilegesOverUser(user, token);
            id = modifyUser(user, body);

        } catch (ResponseException e) {
            return generateErrorResponse(response, e.getMessage(), e.getStatusCode());
        }
        return getUserResponse(Long.toString(id), response);
    }

    private long modifyUser(User user, String body) throws ResponseException {
        long id;
        User newUser = gson.fromJson(body, User.class);
        if (user == null)
            user = newUser;
        else
            user.updateWith(newUser);
        if (user.getUsername() == null || Objects.equals(user.getUsername(), "")) {
            throw new ResponseException("Username can't be null or empty!", 422);
        }
        try {
            id = userGateway.saveUser(user);
        } catch (Exception e) {
            logger.error(Log.getStringStackTrace(e));
            throw new ResponseException("Something went wrong!", 500);
        }
        return id;
    }

    private void checkPrivilegesOverUser(User user, String clientToken) throws ResponseException {
        User client;
        try {
            client = decryptJTW(clientToken);
        } catch (Exception e) {
            logger.error(Log.getStringStackTrace(e));
            throw new ResponseException("Unauthorized", 401);
        }
        if (client.getRole() == 2 && (user == null || user.getUserId() != client.getUserId())) {
            throw new ResponseException("Forbidden", 403);
        }
    }
}
