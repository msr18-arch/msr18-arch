package architecture.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import architecture.commons.files.FileHandler;

public class BuildResultAnalyzer {
	
	public static int NO_ERROR = 0;
	public static int DEPENDENCY_ERROR = 1;
	public static int COMPILATION_ERROR = 2;
	public static int TEST_ERROR = 3;
	public static int UNKNOWN_ERROR = 4;
	
	private String downloadFolder;
	private String project;
	private int failReason;
		
	static Logger log = Logger.getLogger(BuildResultAnalyzer.class);

	public BuildResultAnalyzer(String downloadFolder, String project) {
		
		this.downloadFolder = downloadFolder;
		this.project = project;
	}
	
	public int getFailReason(String jobId) {
		String architectureFolder = FilenameUtils.normalize(downloadFolder + "/" + jobId);
		
		
		
		File textFile;
		try {
			textFile = FileHandler.downloadLog(project, jobId, architectureFolder);
			BufferedReader br = new BufferedReader(new FileReader(textFile));
			String line;
		    while ((line = br.readLine()) != null) {
		    	consumeLine(line);
		    }
		    br.close();
		    
		    FileHandler.remove(textFile.getParentFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return failReason;
	}
	
	private void consumeLine(String line) throws IOException {
		//System.out.println(line);
		if(line.startsWith("[ERROR]")) {
    		setFailReason(UNKNOWN_ERROR);
    		if(line.contains("dependencies")) {
    			setFailReason(DEPENDENCY_ERROR);
    		}
    		if(line.contains("Compilation failure")) {
    			setFailReason(COMPILATION_ERROR);
    		}
    		if(line.contains("There are test failures")) {
    			setFailReason(TEST_ERROR);
    		}
    	}
		if(line.startsWith("[INFO] BUILD SUCCESS")) {
			setFailReason(NO_ERROR);
		}
	}
	
	private void setFailReason(int failReason) {
		if(this.failReason == NO_ERROR 
				|| this.failReason == UNKNOWN_ERROR) {
			//System.out.println("FailReason: " + failReason);
			this.failReason = failReason;
		}
	}

}
