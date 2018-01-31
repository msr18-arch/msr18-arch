package architecture.extraction; 

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import architecture.extraction.classes.AbstractClassGraphExtractor;
import architecture.extraction.reconstruction.AbstractArchitectureReconstructor;

public class SplittedArchitectureExtractor extends AbstractArchitectureExtractor {
	protected AbstractClassGraphExtractor extractor;
	protected AbstractArchitectureReconstructor reconstructor;
	
	private String intermediateFileName;
	
	static Logger log = Logger.getLogger(SplittedArchitectureExtractor.class);
	
	public SplittedArchitectureExtractor(
			AbstractClassGraphExtractor extractor, 
			AbstractArchitectureReconstructor reconstructor,
			String intermediateFileName) {
		
		this.extractor = extractor;
		this.reconstructor = reconstructor;
		
		this.intermediateFileName = intermediateFileName;
	}

	@Override
	public File[] computeArchitecture(File projectFolder, String outputDir) throws IOException {
		new File(outputDir).mkdirs();
		
		File classStructure = computeClasses(projectFolder, outputDir);	
		return new File[]{computeArc(projectFolder, outputDir, classStructure)};
	}
		
	protected File computeClasses(File projectFolder, String outputDir) throws IOException {
		String classStructureName = outputDir + "/" + intermediateFileName + ".rsf";
		File classStructure = new File(FilenameUtils.normalize(classStructureName));
		
		if(!classStructure.exists()) {
			extractor.extract(projectFolder, classStructure);
		} else {
			log.info("Class Structure " + projectFolder.getName() + " already extracted");
		}
		
		return classStructure;
		
	}
	
	protected File computeArc(File projectFolder, String outputDir, File classStructure,
			AbstractArchitectureReconstructor recon) {
		String outputName = outputDir + "/" + recon.getName() + ".rsf";
		File output = new File(FilenameUtils.normalize(outputName));
				
		if(!output.exists()) {
			recon.reconstruct(classStructure, output);	
		} else {
			log.info("Architecture Structure " + projectFolder.getName() + " already extracted");
		}
		
		return output;
	}
	
	protected File computeArc(File projectFolder, String outputDir, File classStructure) {
		return computeArc(projectFolder, outputDir, classStructure, this.reconstructor);
	}

	@Override
	public Optional<File[]> isComputed(String outputDir) {
		String classStructureName = outputDir + "/" + intermediateFileName + ".rsf";
		File classStructure = new File(FilenameUtils.normalize(classStructureName));
				
		String outputName = outputDir + "/" + this.reconstructor.getName() + ".rsf";
		File output = new File(FilenameUtils.normalize(outputName));
		
		if( output.exists() && classStructure.exists()) {
			return Optional.of(new File[]{output});
		} else {
			return Optional.empty();
		}
	}
}
