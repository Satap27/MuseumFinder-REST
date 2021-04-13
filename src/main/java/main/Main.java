package main;

import io.ebean.DB;
import model.Museum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        Museum museum = new Museum.Builder("Hello World")
                .wikiLink("test")
                .website("test")
                .location("test")
                .lat(1.123)
                .lng(3.321)
                .description("description")
                .address("aaa")
                .build();

        // insert the customer in the DB
        DB.save(museum);
        notFound((request, response) -> {
            response.type("application/json");
            return "{\"message\":\"Custom 404\"}";
        });

        internalServerError((request, response) -> {
            response.type("application/json");
            return "{\"message\":\"Custom 500 handling\"}";
        });

        path("/api", () -> {
            before("/*", (q, a) -> logger.info("Received api call"));

            get("/_health", (request, response) -> {
                response.status(204);
                return "";
            });

            path("/museums", () -> {
                get("", (request, response) ->  {
                    response.type("application/json");
                    return "";
                });
            });

            get("/hello/:name", (request, response) ->
                    "Hello " + request.params(":name"));

            /*path("/username", () -> {
                post("/add",       UserApi.addUsername);
                put("/change",     UserApi.changeUsername);
                delete("/remove",  UserApi.deleteUsername);
            });*/
        });
    }
}
