package business_logic;

import model.Museum;
import org.sonatype.inject.Nullable;

import java.util.List;

public class ScoreStrategy implements SearchStrategy {

    @Override
    public List<Museum> filterList(List<Museum> museums, @Nullable String location) {
        return museums;
    }
}
