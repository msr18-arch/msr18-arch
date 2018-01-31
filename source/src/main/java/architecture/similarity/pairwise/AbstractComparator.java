package architecture.similarity.pairwise;

import java.util.Map;

public abstract class AbstractComparator {

	public abstract Map<String, Double> compare(Map<String, Double> m1, Map<String, Double> m2);
	
}
