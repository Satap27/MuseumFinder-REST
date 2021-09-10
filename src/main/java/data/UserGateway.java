package data;

import io.ebean.DB;
import model.User;
import model.query.QMuseum;
import model.query.QUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserGateway {
    private static final Logger logger = LoggerFactory.getLogger(UserGateway.class);
    private static UserGateway instance = null;


    /**
     * Returns the only UserGateway instance (Singleton pattern)
     *
     * @return the UserGateway instance
     */
    public static UserGateway getInstance() {
        logger.info("Retrieving UserGateway instance...");
        if (instance == null) {
            logger.info("UserGateway first initialization...");
            instance = new UserGateway();
        }
        logger.info("UserGateway instance retrieved");
        return instance;
    }

    /**
     * Returns a User object if the user with the given id exists in the database, or <b>null</b> otherwise.
     *
     * @param userId the user id
     * @return a User object
     */
    public User getUser(long userId) {
        return new QUser()
                .userId.eq(userId)
                .ownedMuseums.fetch(QMuseum.alias().name)
                .findOne();
    }

    public long saveUser(User user) {
        DB.save(user);
        return user.getUserId();
    }

    public void removeUser(User user) {
        DB.delete(user);
    }
}
