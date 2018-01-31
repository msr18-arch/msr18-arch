package architecture.extraction.reconstruction;

import java.io.File;

public abstract class AbstractArchitectureReconstructor {
	
	private String name;
	
	public AbstractArchitectureReconstructor(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public abstract void reconstruct(File graph, File output);
}
