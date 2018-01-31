package architecture.extraction;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public abstract class AbstractArchitectureExtractor {

	public abstract File[] computeArchitecture(File projectFolder, String outputDir) throws IOException;
	
	public abstract Optional<File[]> isComputed(String outputDir);

}
