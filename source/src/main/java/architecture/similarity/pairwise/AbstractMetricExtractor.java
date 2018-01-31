package architecture.similarity.pairwise;

import java.io.File;
import java.util.Map;

public abstract class AbstractMetricExtractor {
	
	public abstract Map<String, Double> getMetrics(File arc);
}
