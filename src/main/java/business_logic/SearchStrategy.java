package business_logic;

import org.sonatype.inject.Nullable;

public interface SearchStrategy {
    /**
     * Returns a PostgreSQL query for a full-text search, based on the keywords and an optional location.
     *
     * @param keywords a list of keywords for the full-text search
     * @param location a nullable string with the preferred location
     * @return the select PostgreSQL query
     */
    String buildSelect(String[] keywords, @Nullable String location);
}
