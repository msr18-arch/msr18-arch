package architecture.similarity.pairwise.graph;

import architecture.similarity.pairwise.PairwiseSimilarityComputer;

/*
 * Computed Metrics:
 * Per Module: 
 *   Absolute afferent (inc) / efferent (out) coupling
 *   Absolute Instability (eff / (eff + aff))
 *   General Instability (does not consider value of edge)
 *   degree (number of directly connected nodes)
 * Graph:
 *   average node degree
 *   average abs instability
 *   averge gen instability
 *   number(nodes)
 *   number(edges) 
 * 
 */
public class GraphSimiliarityComputer extends PairwiseSimilarityComputer {

	public static final String NUMBER_NODES = "numNodes";
	public static final String NUMBER_EDGES = "numEdges";
	public static final String AVG_NODE_DEG = "avgNodeDeg";
	public static final String AVG_ABS_INST = "avgAbsInst";
	public static final String AVG_REL_INST = "avgRelInst";
	public static final String DELIMITER = "_";
	public static final String AFFERENT = "aff";
	public static final String EFFERENT = "eff";
	public static final String ABS_INST = "absInst";
	public static final String REL_INST = "relInst";
	public static final String DEG = "degree";

	public GraphSimiliarityComputer() {
		super(new GraphMetricExtractor(), new GraphMetricComparator());
	}

	@Override
	public double getNormalizedDifference(double simValue) {
		return 1 - simValue;
	}

}
