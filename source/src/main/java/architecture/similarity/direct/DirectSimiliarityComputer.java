package architecture.similarity.direct;

import java.util.HashMap;
import java.util.Map;

import architecture.similarity.AbstractArchitectureSimilarityComputer;

public abstract class DirectSimiliarityComputer extends AbstractArchitectureSimilarityComputer {
	
	protected Map<String, Double> metrics;
	
	public DirectSimiliarityComputer() {
		metrics = new HashMap<String, Double>();
	}

}
