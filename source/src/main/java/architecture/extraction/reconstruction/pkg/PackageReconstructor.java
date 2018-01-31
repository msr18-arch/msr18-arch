package architecture.extraction.reconstruction.pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import architecture.extraction.classes.husacct.HusaactGraphFileWriter;
import architecture.extraction.reconstruction.AbstractArchitectureReconstructor;


public class PackageReconstructor extends AbstractArchitectureReconstructor {
		
	private MutableValueGraph<String, String> inputGraph;
	private MutableValueGraph<String, Integer> outputGraph;
	
	static Logger log = Logger.getLogger(PackageReconstructor.class);
	
	public PackageReconstructor(String name) {
		super(name);
		
		inputGraph = ValueGraphBuilder.directed().build();
		outputGraph = ValueGraphBuilder.directed().build();
	}

	@Override
	public void reconstruct(File graph, File output) {
		try (Stream<String> stream = Files.lines(Paths.get(graph.toURI()))) {

			stream.forEach(line -> buildGraph(line));

		} catch (IOException e) {
			e.printStackTrace();
			return;
		} 
		
		log.info("Calculate Clusters");
		getClusters();		
		log.info("Calculate Dependencies");
		addDependencies();
		
		try {
			PackageGraphFileWriter writer = new PackageGraphFileWriter(output);
			writer.write(outputGraph);
		} catch (FileNotFoundException e) {
			System.err.println("Could not print Package Architecture");
			e.printStackTrace();
		}
		
	}
	
	private void buildGraph(String line) {
		String[] parts = line.split(" ");
		if(parts.length != 3 || parts[0].equals("depends")) {
			return;
		}
		
		// Adds nodes as well
		if(!parts[1].equals(parts[2])) {
			inputGraph.putEdgeValue(parts[1], parts[2], parts[0]);
		}
	}
	
	private void getClusters() {
		Set<String> nodes = inputGraph.nodes();		
		
		Set<String> clusters = new HashSet<String>();
				
		for(String node : nodes) {
			if(inputGraph.predecessors(node).isEmpty()) {
				clusters.add(node);
			}
		}
		
		boolean change = true;
		while(change) {
			change = false;
			
			Set<String> tempClusters = new HashSet<String>(clusters);
			for(String node : clusters) {
				Set<String> successors = inputGraph.successors(node);							
				if(!isPackage(node)) {
					tempClusters.remove(node);
				} else {
					String firstSucc = successors.iterator().next();
					// If only one element, take successor package
					if(successors.size() == 1 && !containsClasses(node)) {
						tempClusters.remove(node);
						tempClusters.add(firstSucc);
						change = true;
					}
				}
			}
			clusters = tempClusters;
		}
		
		
		change = true;
		while(clusters.size() < 10 && change) {
			change = false;
			//System.out.println(clusters.size());
			Set<String> tempClusters = new HashSet<String>(clusters);
			for(String node : clusters) {
				if(!containsSubPkg(node)) {
					continue;
				}
				change = true;
				tempClusters.remove(node);
				for(String succ: inputGraph.successors(node)) {
					if(isPackage(succ)) {
						tempClusters.add(succ);
					}
				}			
			}
			clusters = tempClusters;
		}
		
		
		for(String node: clusters) {
			outputGraph.addNode(node);
		}
		
	}
	
	private void addDependencies() {
		Set<EndpointPair<String>> edges = this.inputGraph.edges();
		for(EndpointPair<String> pair : edges) {
			String type = inputGraph.edgeValueOrDefault(pair.source(), pair.target(), null);
			if(type.equals(HusaactGraphFileWriter.REFERENCES)) {
				String source;
				String target;
				
				//System.out.println(pair.source() + " =>" + pair.target());
				
				if(outputGraph.nodes().contains(pair.source())) {
					source = pair.source();
				} else {
					source = findParent(pair.source());
				}
				
				if(outputGraph.nodes().contains(pair.target())) {
					target = pair.target();
				} else {
					target = findParent(pair.target());
				}
				
				if(source != null && target != null && !source.equals(target)) {
					int oldEdgeValue = outputGraph.edgeValueOrDefault(source, target, 0);
					outputGraph.putEdgeValue(source, target, oldEdgeValue+1);
					//System.out.println(source + " -> " + target);
				}
				
			}
		}
	}
	
	private boolean containsClasses(String node) {
		Set<String> successors = inputGraph.successors(node);
		for(String succ : successors) {
			if(!isPackage(succ)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsSubPkg(String pkg) {
		Set<String> successors = inputGraph.successors(pkg);
		for(String succ : successors) {
			if(isPackage(succ)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isPackage(String node) {
		boolean isNotEmpty;
		Set<String> successors = inputGraph.successors(node);
		if(!successors.iterator().hasNext()) {
			isNotEmpty = false;
		} else {
			String firstSucc = successors.iterator().next();
			isNotEmpty = firstSucc.contains(node);
		}
		
		boolean hasPkgTypeRelations = false;
		Set<String> predecessors = inputGraph.predecessors(node);
		if(!predecessors.iterator().hasNext()) {
			hasPkgTypeRelations = true;
		} else {
			for(String pred : predecessors) {
				if(inputGraph.edgeValueOrDefault(pred, node, "").equals(HusaactGraphFileWriter.SUB_PKG)) {
					hasPkgTypeRelations = true;
				}
			}
		}
		return isNotEmpty && hasPkgTypeRelations;
		
	}
	
	private String findParent(String node) {
		Set<String> predecessors = inputGraph.predecessors(node);
		Optional<String> pkg = Optional.empty();
		for(String pred: predecessors) {
			if(inputGraph.edgeValueOrDefault(pred, node, null).equals(HusaactGraphFileWriter.CONTAINS) || 
					inputGraph.edgeValueOrDefault(pred, node, null).equals(HusaactGraphFileWriter.SUB_PKG)) {
				pkg = Optional.of(pred);
				break;
			}
		}
		
		if(!pkg.isPresent()) {
			return null;
		}
		
		if(outputGraph.nodes().contains(pkg.get())) {
			return pkg.get();
		} else {
			return findParent(pkg.get());
		}
	}

}
 