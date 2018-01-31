package architecture.similarity.pairwise.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import architecture.similarity.pairwise.AbstractComparator;

public class GraphMetricComparator extends AbstractComparator {
	
	private Map<String, Double> res;
	
	@Override
	public Map<String, Double> compare(Map<String, Double> m1, Map<String, Double> m2) {
		Set<String> metrics = new HashSet<String>(m1.keySet());
		res = new HashMap<String, Double>();
		metrics.addAll(m2.keySet());
		for(String metric: metrics) {
			switch (metric) {
				case GraphSimiliarityComputer.NUMBER_NODES: compareSimMetric(metric, m1, m2, true);
				continue;
				case GraphSimiliarityComputer.NUMBER_EDGES: compareSimMetric(metric, m1, m2, true);
				continue;
				case GraphSimiliarityComputer.AVG_NODE_DEG: compareSimMetric(metric, m1, m2, true);
				continue;
				case GraphSimiliarityComputer.AVG_ABS_INST: compareSimMetric(metric, m1, m2, false);
				continue;
				case GraphSimiliarityComputer.AVG_REL_INST: compareSimMetric(metric, m1, m2, false);
				continue;
			}
			
			if(metric.contains(GraphSimiliarityComputer.DELIMITER + GraphSimiliarityComputer.AFFERENT)) {
				compareSimMetric(metric, m1, m2, true);
			} else if(metric.contains(GraphSimiliarityComputer.DELIMITER + GraphSimiliarityComputer.EFFERENT)) {
				compareSimMetric(metric, m1, m2, true);
			} else if(metric.contains(GraphSimiliarityComputer.DELIMITER + GraphSimiliarityComputer.ABS_INST)) {
				compareSimMetric(metric, m1, m2, false);
			} else if(metric.contains(GraphSimiliarityComputer.DELIMITER + GraphSimiliarityComputer.REL_INST)) {
				compareSimMetric(metric, m1, m2, false);
			} else if(metric.contains(GraphSimiliarityComputer.DELIMITER + GraphSimiliarityComputer.DEG)) {
				compareSimMetric(metric, m1, m2, true);
			} else {
				System.err.println("Unknown metric: " + metric);
			}
				
		}
		
		
		return res;
	}
	
	private void compareSimMetric(String metric, Map<String, Double> m1, Map<String, Double> m2, boolean abs) {
		res.put(metric, calcSimMetric(metric, m1, m2, abs));
	}
		
	private double calcSimMetric(String metric, Map<String, Double> m1, Map<String, Double> m2, boolean abs) {
		double v1 = m1.getOrDefault(metric, -1.0);
		double v2 = m2.getOrDefault(metric, -1.0);
		
		// If no value is present, metrics are unequals
		if(v1 == -1 || v2 == -1) {
			return 0.0;
		}
		
		if(abs) {
			return computeSimAbsoluteValues(v1, v2);
		} else {
			return computeSimRelativeValues(v1, v2);
		}
		
		
	}
	
	private double computeSimAbsoluteValues(double v1, double v2) {
		//Division by 0 Check
		if(v1 == 0 && v2 == 0) {
			return 1.0;
		}
		
		return (double) Math.min(v1, v2) / Math.max(v1, v2);
	}
	
	private double computeSimRelativeValues(double v1, double v2) {
		return (double) 1- (Math.max(v1, v2) - Math.min(v1, v2));
	}

}
