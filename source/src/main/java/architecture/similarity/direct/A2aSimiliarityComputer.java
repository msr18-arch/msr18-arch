package architecture.similarity.direct;

import java.io.File;
import java.util.Map;

import edu.usc.softarch.arcade.metrics.BatchSystemEvo;


public class A2aSimiliarityComputer extends DirectSimiliarityComputer {

	public A2aSimiliarityComputer() {
		super();
	}
	
	@Override
	public Map<String, Double> computeSimilarity(File arcOne, File arcTwo) {
		metrics.put("a2a", BatchSystemEvo.computeSysEvo(arcOne, arcTwo));
		return metrics;
	}
	
	@Override
	public double getNormalizedDifference(double simValue) {
		return 1 - (simValue / 100);
	}

}
