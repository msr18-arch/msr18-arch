package architecture.commons.files;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

public class FileHandler {
	
	public final static String FORMAT = ".tar.gz";
	public final static String TXT_ENDING = ".txt";
	
	static Logger log = Logger.getLogger(FileHandler.class);
	
	public static File downloadCommit(String project, String commit, String folder) throws IOException {
		URL path = new URL("https://github.com/" + project + "/archive/" + commit + FORMAT);
		
		String[] separated = project.split("/");
		String author = separated[0];
		String projectName = separated[1];
		
		String filePath = folder + "/" + author + "-" + projectName + "-" + commit + FORMAT;
		filePath = FilenameUtils.normalize(filePath);
		
		File file = new File(filePath);
		if(file.exists()) {
			log.info(" Archive " + path + " was already downloaded");
			return file;
		}
		
		FileUtils.copyURLToFile(path, file);
		return file;
	}
	
	public static File downloadLog(String project, String jobId, String folder) throws IOException {
		URL path = new URL("https://api.travis-ci.org/v3/job/" + jobId + "/log.txt");
		
		String[] separated = project.split("/");
		String author = separated[0];
		String projectName = separated[1];
		
		String filePath = folder + "/" + author + "-" + projectName + "-" + jobId + TXT_ENDING;
		
		filePath = FilenameUtils.normalize(filePath);
		
		File file = new File(filePath);
		if(file.exists()) {
			log.info(" Archive " + path + " was already downloaded");
			return file;
		}
		
		FileUtils.copyURLToFile(path, file);
		return file;
	}
	
	public static File extract(File archive, String destinationFolder) throws IOException {
		// Twice because of tar and gz
		String commit = FilenameUtils.getBaseName(FilenameUtils.getBaseName(archive.getName()));
		
		
		String destPath = destinationFolder + "/" + commit + "/";
		destPath = FilenameUtils.normalize(destPath);
		File dest = new File(destPath);
		
		if(dest.exists()) {
			log.info("Archive " + archive.getName() + " was already extracted");
		} else {
			Archiver archiver = ArchiverFactory.createArchiver(archive);
			archiver.extract(archive, dest);
		}
		
		// Go into subdirectory if archive was only one dir
		if(dest.listFiles().length == 2) {
			for(File file : dest.listFiles()) {
				if(file.isDirectory()) {
					return file;
				}
			}
		}		
		return dest;
	}
	
	public static void remove(File file) throws IOException {
		FileUtils.deleteQuietly(file);
	}
}
