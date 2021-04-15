package application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Nullable;

public class ScoreStrategy implements SearchStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ScoreStrategy.class);


    @Override
    public String buildSelect(String[] keywords, @Nullable String location) {
        logger.info("Building SQL query based on score...");
        String query = "SELECT to_jsonb(array_agg(list)) AS json FROM (SELECT  name, museum_id FROM (SELECT (";
        for (String keyword : keywords) {
            query = query.concat(String.format("ts_rank_cd(P.description_tsv, to_tsquery('italian', '%s'))/(1 + " +
                            "(SELECT sum(ts_rank_cd(P.description_tsv, to_tsquery('italian', '%s'))) FROM museum as P)) + ",
                    keyword, keyword));
        }
        query = query.substring(0, query.length() - 3).concat(") AS score, P.name, P.museum_id FROM museum as P) S WHERE score > 0 " +
                "ORDER BY score DESC LIMIT 50) list;");
        logger.debug("QUERY:" + query);
        return query;
    }
}
