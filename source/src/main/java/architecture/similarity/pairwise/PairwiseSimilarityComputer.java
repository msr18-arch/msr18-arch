package architecture.similarity.pairwise;

import java.io.File;
import java.util.Map;

import architecture.similarity.AbstractArchitectureSimilarityComputer;

public abstract class PairwiseSimilarityComputer extends AbstractArchitectureSimilarityComputer {
	
	protected AbstractMetricExtractor metricExtractor;
	protected AbstractComparator comparator;
	
	public PairwiseSimilarityComputer(
			AbstractMetricExtractor metricExtractor, AbstractComparator comparator) {
		this.metricExtractor = metricExtractor;
		this.comparator = comparator;
	}
	
	@Override
	public Map<String, Double> computeSimilarity(File arcOne, File arcTwo) {
		Map<String, Double> m1 = metricExtractor.getMetrics(arcOne);
		Map<String, Double> m2 = metricExtractor.getMetrics(arcTwo);
						
		return comparator.compare(m1, m2);
	}
	
	
}
