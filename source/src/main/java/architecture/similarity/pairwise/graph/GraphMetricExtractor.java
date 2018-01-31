package architecture.similarity.pairwise.graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import architecture.similarity.pairwise.AbstractMetricExtractor;

public class GraphMetricExtractor extends AbstractMetricExtractor{
	
	private MutableValueGraph<String, Integer> architecture;
	private Map<String, Double> res;
	
	public GraphMetricExtractor() {
		architecture = ValueGraphBuilder.directed().build();
	}
	
	@Override
	public Map<String, Double> getMetrics(File arc) {		
		res = new HashMap<String, Double>();
		
		try (Stream<String> stream = Files.lines(Paths.get(arc.toURI()))) {
			stream.forEach(line -> buildGraph(line));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
				
		double numNodes = (double) architecture.nodes().size();
		res.put(GraphSimiliarityComputer.NUMBER_NODES, numNodes);
		double numEdges = (double) architecture.edges().size();
		res.put(GraphSimiliarityComputer.NUMBER_EDGES, numEdges);
		
		
		for(String node : architecture.nodes()) {
			computeNodeMetrics(node);
		}
		
		double degSum = 0;
		double absInstSum = 0;
		double resInstSum = 0;
		for(Entry<String, Double> metric : res.entrySet()) {
			if(metric.getKey().contains(GraphSimiliarityComputer.DELIMITER + GraphSimiliarityComputer.DEG)) {
				degSum += metric.getValue();
			} else if(metric.getKey().contains(GraphSimiliarityComputer.DELIMITER + GraphSimiliarityComputer.ABS_INST)) {
				absInstSum += metric.getValue();
			} else if(metric.getKey().contains(GraphSimiliarityComputer.DELIMITER + GraphSimiliarityComputer.REL_INST)) {
				resInstSum += metric.getValue();
			}
		}
		res.put(GraphSimiliarityComputer.AVG_NODE_DEG, degSum / numNodes);
		res.put(GraphSimiliarityComputer.AVG_ABS_INST, absInstSum / numNodes);
		res.put(GraphSimiliarityComputer.AVG_REL_INST, resInstSum / numNodes);
		
		return res;
	}
	
	private void computeNodeMetrics(String node) {
		String delimitedNodeName = node + GraphSimiliarityComputer.DELIMITER;
		
		Set<String> succ = architecture.successors(node);
		Set<String> pred = architecture.predecessors(node);

		int eff = countEdgeValue(node, succ, true);
		int aff = countEdgeValue(node, pred, false);
		int degree = architecture.adjacentNodes(node).size();		
		double absInst = (double) eff / (eff + aff);
		double relInst = (double) succ.size() / (succ.size() + pred.size());
				
		res.put(delimitedNodeName + GraphSimiliarityComputer.EFFERENT, (double) eff);
		res.put(delimitedNodeName + GraphSimiliarityComputer.AFFERENT, (double) aff);
		res.put(delimitedNodeName + GraphSimiliarityComputer.ABS_INST, absInst);
		res.put(delimitedNodeName + GraphSimiliarityComputer.REL_INST, relInst);
		res.put(delimitedNodeName + GraphSimiliarityComputer.DEG, (double) degree);
		
	}
	
	private int countEdgeValue(String node, Set<String> neighbors, boolean out) {
		int res = 0;
		for(String neighbor : neighbors) {
			if(out) {
				res += architecture.edgeValueOrDefault(node, neighbor, 0);
			} else {
				res += architecture.edgeValueOrDefault(neighbor, node, 0);
			}
		}
		return res;
	}
	
	private void buildGraph(String line) {
		String[] parts = line.split(" ");
		if(parts.length != 3) {
			return;
		}
				
		int value;
		try {
			value = Integer.parseInt(parts[0]);
		} catch(NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		// Adds nodes as well
		architecture.putEdgeValue(parts[1], parts[2], value);
	}

}
