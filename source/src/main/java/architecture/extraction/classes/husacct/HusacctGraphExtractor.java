package architecture.extraction.classes.husacct;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.jdom2.Element;

import architecture.extraction.classes.AbstractClassGraphExtractor;
import husacct.analyse.AnalyseServiceImpl;
import husacct.analyse.IAnalyseService;
import husacct.common.dto.ProjectDTO;

public class HusacctGraphExtractor extends AbstractClassGraphExtractor {

	public static final String[] SOURCE_DIR = {"src", "main"};
	
	private boolean simpleWriter;
		
	public HusacctGraphExtractor(boolean simpleWriter) {
		this.simpleWriter = simpleWriter;
	}
	
	@Override
	public void extract(File projectFolder, File output) throws IOException {		
		Element res = extractGraph(projectFolder);
		
		HusaactGraphFileWriter writer = new HusaactGraphFileWriter(output, simpleWriter);
		writer.write(res);
	}
	
	
	private Element extractGraph(File root) {
		List<String> res = getSourceFolders(root);
		Element graph = extract(res);
		
		return graph;
	}
		
	private List<String> removeTestFolders(List<String> list) {
		List<String> withoutTest = new ArrayList<String>();
		
		for(String item : list) {
			if(!item.contains("test")) {
				withoutTest.add(item);
			}
		}
		
		return withoutTest;
	}
	
	private List<String> getSourceFolders(File root) {				
		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model;
		String[] sources = SOURCE_DIR;
		
		try {
			String path = root.getAbsolutePath() + "/pom.xml";
			model = reader.read(new FileReader(path));
			String sourcePath = model.getBuild().getSourceDirectory();
			if(sourcePath != null) {
				sources = sourcePath.split("/");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> result = getSourceFolders(root, sources);
		return removeTestFolders(result); 
    }
	
	private List<String> getSourceFolders(File root, String[] sources) {
		List<String> result = new ArrayList<String>();

		  for (File file : root.listFiles()) {
		    if (file.isDirectory()) {
		      if (file.getName().equals(sources[1]) && 
		        file.getParentFile().getName().equals(sources[0])) {
		    	  result.add(file.getPath());
		      } else {
		    	  result.addAll(getSourceFolders(file, sources));
		      }
		    }
		  }

		  return result;
	}
	
	
	private static synchronized Element extract(List<String> path) {
		IAnalyseService analyseService = new AnalyseServiceImpl();
	    
	    ProjectDTO project = new ProjectDTO("TestName", new ArrayList<String>(path), "Java", "1.0", "",
	        null);
	    
	    analyseService.analyseApplication(project);
	    
	    Element result = analyseService.exportAnalysisModel();
	    
	    return result;
	   
	}

}
