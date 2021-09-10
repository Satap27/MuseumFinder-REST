package business_logic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import data.MuseumGateway;
import data.UserGateway;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import model.Log;
import model.Message;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

public class Controller {
    protected static final Logger logger = LoggerFactory.getLogger(Controller.class);
    protected final MuseumGateway museumGateway = MuseumGateway.getInstance();
    protected final UserGateway userGateway = UserGateway.getInstance();
    protected final Gson gson = new Gson();


    protected User getUser(String userId) throws ResponseException {
        long parsedId;
        try {
            parsedId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            logger.error(Log.getStringStackTrace(e));
            throw new ResponseException("Provided user id is not a number!", 422);
        }
        User user;
        try {
            user = userGateway.getUser(parsedId);
        } catch (Exception e) {
            logger.error(Log.getStringStackTrace(e));
            throw new ResponseException("Something went wrong!", 500);
        }
        if (user == null) {
            logger.warn("User with id " + userId + " not found!");
            throw new ResponseException("User not found.", 404);
        }
        return user;
    }

    protected User decryptJTW(String token) throws Exception {
        Base64.Decoder decoder = Base64.getDecoder();
        String[] chunks = token.replace("Bearer ", "").split("\\.");
        String payload = new String(decoder.decode(chunks[1]));
        SignatureAlgorithm signatureAlgorithm = HS256;
        String secretKey = System.getenv("JWT_SECRET_KEY");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), signatureAlgorithm.getJcaName());
        String tokenWithoutSignature = chunks[0] + "." + chunks[1];
        String signature = chunks[2];
        DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(signatureAlgorithm, secretKeySpec);
        if (!validator.isValid(tokenWithoutSignature, signature)) {
            throw new Exception("Could not verify JWT token integrity!");
        }
        JsonObject jsonObject = gson.fromJson(payload, JsonObject.class);
        return userGateway.getUser(jsonObject.get("sub").getAsLong());
    }

    protected JsonElement encapsulateJsonObject(JsonElement jsonElement, String name, String json) {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        jsonElement.getAsJsonObject().add(name, gson.toJsonTree(jsonObject));
        return jsonElement;
    }

    protected JsonElement encapsulateJsonArray(JsonElement jsonElement, String name, String json) {
        JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
        jsonElement.getAsJsonObject().add(name, gson.toJsonTree(jsonArray));
        return jsonElement;
    }

    protected Response generateErrorResponse(Response response, String messageText, int statusCode) {
        Message message = new Message();
        message.setMessage(messageText);
        response.body(gson.toJson(message));
        response.status(statusCode);
        return response;
    }
}

class ResponseException extends Exception {
    String message;
    int statusCode;
    public ResponseException (String message, int statusCode){
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
