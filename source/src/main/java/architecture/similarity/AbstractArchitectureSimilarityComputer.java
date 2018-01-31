package architecture.similarity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractArchitectureSimilarityComputer {
	
	public abstract Map<String, Double> computeSimilarity(File arcOne, File arcTwo);
	
	public abstract double getNormalizedDifference(double simValue);
	
	public Map<String, Double> computeDifference(File arcOne, File arcTwo) {
		Map<String, Double> res = new HashMap<String, Double>();
		for(Entry<String, Double> sim : computeSimilarity(arcOne, arcTwo).entrySet()) {
			res.put(sim.getKey(), getNormalizedDifference(sim.getValue()));
		}
		return res;
	}
	
}
