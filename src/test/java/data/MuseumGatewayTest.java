package data;

import io.ebean.DB;
import io.ebean.Database;
import model.Museum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MuseumGatewayTest {
    private static final Logger logger = LoggerFactory.getLogger(MuseumGatewayTest.class);

    @BeforeAll
    static void initialize() {
        logger.info("Initializing database with a test museum");
        Museum museum = new Museum.Builder("test")
                .description("description")
                .build();
        DB.save(museum);
    }

    @Test
    public void singletonTest() {
        logger.info("Checking if getInstance method returns always the same instance...");
        MuseumGateway museumGateway1 = MuseumGateway.getInstance();
        MuseumGateway museumGateway2 = MuseumGateway.getInstance();
        Assertions.assertEquals(museumGateway1, museumGateway2);
    }

    @Test
    public void getMuseumTest() {
        MuseumGateway museumGateway = MuseumGateway.getInstance();
        logger.info("Searching museum by id...");
        Museum museum = museumGateway.getMuseum(1);
        Assertions.assertNotNull(museum);
        museum = museumGateway.getMuseum(2);
        Assertions.assertNull(museum);
    }
}
