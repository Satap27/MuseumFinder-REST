package gateway;

import io.ebean.DB;
import io.ebean.Database;
import model.Museum;
import org.junit.jupiter.api.Test;

public class EbeanTest {
    Database database = DB.getDefault();

    @Test
    public void insertFindDelete() {

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

        // Find by Id
        Museum foundHello = database.find(Museum.class, 1);

        // delete the customer
        DB.delete(museum);
    }
}
