package presentation;

import application.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Controller controller = new Controller();

    public static void main(String[] args) {

        before("/*", (request, response) -> {
            if (!request.ip().equals("127.0.0.1")) {
                logger.warn("Received request from ip " + request.ip() + "!");
                halt(401);
            }
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
                return controller.searchMuseums(request.body());
            });

            path("/museum", () -> get("/:museumId", (request, response) -> {
                response.type("application/json");
                return controller.getMuseum(request.params(":museumId"));
            }));

            //TODO is this working?
            after((request, response) -> response.header("Content-Encoding", "gzip"));
        });
    }
}
