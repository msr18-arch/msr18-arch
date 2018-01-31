package architecture.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import architecture.commons.Compilable;
import architecture.commons.files.JSONFileHandler;

public class CompilableList {
	private JSONFileHandler<Compilable> compileHandler;
	private List<Compilable> compilablelist;
	
	private String path;
	
	static Logger log = Logger.getLogger(CompilableList.class);
	
	public CompilableList(String path) {
		compileHandler = new JSONFileHandler<Compilable>();
		
		this.path = path;
		try {
			loadJSON();
		} catch (IOException e) {
			log.warn("Compilable JSON could not be loaded");
			compilablelist = Collections.synchronizedList(new ArrayList<Compilable>());
		}
	}
	
	public void loadJSON() throws IOException {
		compilablelist = Collections.synchronizedList(compileHandler.readJson(Compilable.class, path));
	}
	
	
	public void storeJSON() throws IOException {
		compileHandler.writeJson(path, compilablelist);
	}

	public synchronized boolean contains(String commitID) {
		for(Compilable comp : compilablelist) {
			if(comp.getCommitID().equals(commitID)) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean add(Compilable compilable) {
		return compilablelist.add(compilable);
		
	}
	
}
