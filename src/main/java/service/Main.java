package service;

import business_logic.MuseumController;
import business_logic.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final MuseumController museumController = new MuseumController();
    private static final UserController userController = new UserController();

    public static void main(String[] args) {

        before("/*", (request, response) -> {
            if (!request.ip().equals("127.0.0.1")) {
                logger.warn("Received request from ip " + request.ip() + "!");
                halt(401);
            }
        });

        after((request, response) -> {
            //response.header("Content-Encoding", "gzip");
        });

        notFound((request, response) -> "");
        internalServerError((request, response) -> "");

        path("/api", () -> {
            before("/*", (request, response) -> logger.info("Received api call"));

            get("/_health", (request, response) -> {
                response.status(204);
                return "";
            });

            get("/museums", (request, response) -> {
                response.type("application/json");
                return museumController.searchMuseums(request.body(), response);
            });

            path("/museum", () -> {
                get("/:museumId", (request, response) -> {
                    response.type("application/json");
                    return museumController.getMuseumResponse(request.params(":museumId"), response);
                });

                put("/:museumId/owner", (request, response) -> {
                    response.type("application/json");
                    return museumController.addOwner(request.params(":museumId"), "2", response, request.headers("Authorization"));
                });

                put("/:museumId", (request, response) -> {
                    response.type("application/json");
                    return museumController.saveMuseumResponse(request.body(), response, request.params(":museumId"), request.headers("Authorization"));
                });

                post("", (request, response) -> {
                    response.type("application/json");
                    return museumController.saveMuseumResponse(request.body(), response, null, request.headers("Authorization"));
                });

            });

            path("/user", () -> {
                get("/:userId", (request, response) -> {
                    response.type("application/json");
                    return userController.getUserResponse(request.params(":userId"), response);
                });

                put("/:userId", (request, response) -> {
                    response.type("application/json");
                    return userController.saveUserResponse(request.body(), response, request.params(":userId"), request.headers("Authorization"));
                });

                post("", (request, response) -> {
                    response.type("application/json");
                    return userController.saveUserResponse(request.body(), response, null, request.headers("Authorization"));
                });

            });

            /*post("/init", (request, response) -> {
                response.type("application/json");
                controller.initializeIndex();
                return "{'message': 'ok'}";
            });*/
        });
    }
}
