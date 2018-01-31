package architecture.main;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import architecture.commons.files.FileHandler;
import architecture.extraction.AbstractArchitectureExtractor;

public class CommitToArchitecture {
	private AbstractArchitectureExtractor extractor;

	private String project;
	private String downloadFolder;
	private String arcFolder;
	
	static Logger log = Logger.getLogger(CommitToArchitecture.class);
	
	public CommitToArchitecture(String downloadFolder, String arcFolder, String project) {
		extractor = Factory.createExtractor();
		
		this.downloadFolder = downloadFolder;
		this.arcFolder = arcFolder;
		this.project = project;
	}
	
	public File[] downloadAndReconstruct(String commitID, boolean removeArchive) throws IOException {
		String architectureFolder = FilenameUtils.normalize(arcFolder + "/" + project + "/" + commitID);
		
		if(extractor.isComputed(architectureFolder).isPresent()) {
			log.info("Architecture already calculated");
			return extractor.isComputed(architectureFolder).get();
		}
		
		if(new File(architectureFolder).exists() && new File(architectureFolder).list().length != 0) {
			
		}
		
		log.info("Download Commit");
		File archive = FileHandler.downloadCommit(project, commitID, downloadFolder);
		File source = FileHandler.extract(archive, downloadFolder);
		
		File[] archictecture = extractor.computeArchitecture(source, architectureFolder);
		
		if(removeArchive) {
			FileHandler.remove(archive);
			FileHandler.remove(source);
			FileHandler.remove(source.getParentFile());
		}
		
		return archictecture;
		
	}
	
}
