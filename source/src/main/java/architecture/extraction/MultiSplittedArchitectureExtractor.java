package architecture.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import architecture.extraction.classes.AbstractClassGraphExtractor;
import architecture.extraction.reconstruction.AbstractArchitectureReconstructor;

public class MultiSplittedArchitectureExtractor extends SplittedArchitectureExtractor {

	private AbstractArchitectureReconstructor[] reconstructors;
	
	static Logger log = Logger.getLogger(MultiSplittedArchitectureExtractor.class);
	
	public MultiSplittedArchitectureExtractor(
			AbstractClassGraphExtractor extractor,
			AbstractArchitectureReconstructor[] reconstructors, String intermediateFileName) {
		super(extractor, null, intermediateFileName);	
		this.reconstructors = reconstructors;
		if(reconstructors.length == 0) {
			System.err.println("No reconstructor give");
			return;
		} else {
			reconstructor = reconstructors[0];
		}
	}
	
	@Override
	public File[] computeArchitecture(File projectFolder, String outputDir) throws IOException {
		new File(outputDir).mkdirs();
		
		log.info("Extract Class Diagram");
		File classStructure = computeClasses(projectFolder, outputDir);
		
		File[] res = new File[reconstructors.length];
		
		for(int i = 0; i < reconstructors.length; i++) {
			log.info("Compute " + i + "-th Architecture");
			res[i] = computeArc(projectFolder, outputDir, classStructure, reconstructors[i]);
		}
		
		return res;
	}
	
	@Override
	public Optional<File[]> isComputed(String outputDir) {
		Optional<File[]> superRes = super.isComputed(outputDir);
		if(superRes.isPresent()) {
			List<File> res = new ArrayList<File>();
			for(AbstractArchitectureReconstructor recon : reconstructors) {
				String outputName = outputDir + "/" + recon.getName() + ".rsf";
				File output = new File(FilenameUtils.normalize(outputName));
				res.add(output);
				
				if(!output.exists()) {
					return Optional.empty();
				}
			}
			return Optional.of(res.toArray(new File[res.size()]));
		}
		return superRes;
		
	}
	
}
