package architecture.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import architecture.commons.VersionDifference;
import architecture.commons.files.JSONFileHandler;
import architecture.similarity.AbstractArchitectureSimilarityComputer;
import architecture.similarity.pairwise.graph.GraphSimiliarityComputer;

public class CompareAndSave {
	
	private JSONFileHandler<VersionDifference> handler;
	//private AbstractArchitectureSimilarityComputer a2aComp;
	//private AbstractArchitectureSimilarityComputer cvgComp;
	//private AbstractArchitectureSimilarityComputer pkgComp;
	
	private String path;
	private List<VersionDifference> list;
	
	static Logger log = Logger.getLogger(CompareAndSave.class);
		
	public CompareAndSave(String path) {
		handler = new JSONFileHandler<VersionDifference>();
//		a2aComp = Factory.createA2aComputer();
//		cvgComp = Factory.createCvgComputer();
//		pkgComp = Factory.createPkgComputer();
		
		this.path = path;
		try {
			loadJSON();
		} catch (IOException e) {
			log.warn("Version Diff JSON could not be loaded");
			list = Collections.synchronizedList(new ArrayList<VersionDifference>());
		}
	}
	
	public void loadJSON() throws IOException {
		list = (List<VersionDifference>) Collections.synchronizedList(handler.readJson(VersionDifference.class, path));
	}
	
	public void storeJSON() throws IOException {
		handler.writeJson(path, list);
	}
	
	public boolean prevCalclulated(int buildNum) {
		for(VersionDifference vd : list) {
			if(vd.getToVersion() == buildNum) {
				return true;
			}
		}
		return false;
	}
	
	public boolean nextCalclulated(int buildNum) {
		for(VersionDifference vd : list) {
			if(vd.getFromVersion() == buildNum) {
				return true;
			}
		}
		return false;
	}
	
	public boolean metricsCalculated(int buildNum) {
		boolean prev = false;
		boolean next = false;
		for(VersionDifference vd : list) {
			if(vd.getFromVersion() == buildNum) {
				next = true;
			}
			if(vd.getToVersion() == buildNum) {
				prev = true;
			}
			if(next && prev) {
				return true;
			}
		}
		return next && prev;	
	}
	
	public Map<String, Map<String, Double>> compare(Pair<Integer, File[]> arcOne, Pair<Integer, File[]> arcTwo) {
		Optional<VersionDifference> vd = listContains(arcOne.getLeft(), arcTwo.getLeft());
		if(vd.isPresent()) {
			log.info("Diff between " + arcOne.getLeft() + " and " + arcTwo.getLeft() + " already computed");
			return vd.get().getMetrics();
		}
		
		AbstractArchitectureSimilarityComputer a2aComp = Factory.createA2aComputer();
		AbstractArchitectureSimilarityComputer cvgComp = Factory.createCvgComputer();
		AbstractArchitectureSimilarityComputer pkgComp = Factory.createPkgComputer();
		
		Map<String, Map<String, Double>> metrics = new HashMap<String, Map<String, Double>>();
		
		for(int i = 0; i < arcOne.getRight().length; i++) {
			switch (FilenameUtils.getBaseName(arcOne.getRight()[i].getAbsolutePath())) {			
			case Factory.ACDC_FILE_BASE: 
				log.info("Computing a2a");
				Map<String, Double> a2aMetrics = 
						a2aComp.computeDifference(arcOne.getRight()[i], arcTwo.getRight()[i]);
				log.info("Compute cvg");
				Map<String, Double> cvgMetrics = 
						cvgComp.computeDifference(arcOne.getRight()[i], arcTwo.getRight()[i]);
				Map<String, Double> combined = new HashMap<String, Double>();
				combined.putAll(a2aMetrics);
				combined.putAll(cvgMetrics);
				metrics.put("arcade", combined);
				break;
			case Factory.PKG_FILE_BASE:
				log.info("Computing Package Metrics");
				Map<String, Double> pkgMetrics =
						pkgComp.computeDifference(arcOne.getRight()[i], arcTwo.getRight()[i]);
				
				Map<String, Double> globalMetrics = new HashMap<String, Double>();
				Map<String, Double> classMetrics = new HashMap<String, Double>(pkgMetrics);
				for(Entry<String, Double> metric: pkgMetrics.entrySet()) {
					if(metric.getKey() == GraphSimiliarityComputer.NUMBER_NODES ||
							metric.getKey() == GraphSimiliarityComputer.NUMBER_EDGES ||
							metric.getKey() == GraphSimiliarityComputer.AVG_NODE_DEG ||
							metric.getKey() == GraphSimiliarityComputer.AVG_ABS_INST ||
							metric.getKey() == GraphSimiliarityComputer.AVG_REL_INST) {
							globalMetrics.put(metric.getKey(), metric.getValue());
							classMetrics.remove(metric.getKey());
					}
				}
				
				metrics.put("global", globalMetrics);
				metrics.put("class", classMetrics);
			}
		}
		list.add(new VersionDifference(arcOne.getLeft(), arcTwo.getLeft(), metrics));
		return metrics;
		
		
	}
	
	private Optional<VersionDifference> listContains(int from, int to) {
		for(VersionDifference vd : list) {
			if(vd.getFromVersion() == from && vd.getToVersion() == to) {
				return Optional.of(vd);
			}
		}
		return Optional.empty();
	}
	
}
