package business_logic;

import model.Museum;
import org.sonatype.inject.Nullable;

import java.util.List;

public interface SearchStrategy {
    /**
     * Returns a PostgreSQL query for a full-text search, based on the keywords and an optional location.
     *
     * @param museums a list of museums
     * @param location a nullable string with the preferred location
     * @return the filtered list of museums
     */
    List<Museum> filterList(List<Museum> museums, @Nullable String location);
}
