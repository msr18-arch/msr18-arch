package architecture.extraction.reconstruction;

import java.io.File;

import acdc.ACDC;

public class ACDCReconstructor extends AbstractArchitectureReconstructor {

	public ACDCReconstructor(String name) {
		super(name);
	}

	@Override
	public void reconstruct(File graph, File outputFile) {
		
		String outputFileName = outputFile.getAbsolutePath();
		String inputFileName = graph.getAbsolutePath();
		
		String[] acdcArgs = {inputFileName, outputFileName};
		
		ACDC.main(acdcArgs);
	}

}
