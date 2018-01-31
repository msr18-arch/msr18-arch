package architecture.extraction.classes;

import java.io.File;
import java.io.IOException;

public abstract class AbstractClassGraphExtractor {
	
	public abstract void extract(File projectFolder, File output) throws IOException;
}
