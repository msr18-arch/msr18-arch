package architecture.extraction.reconstruction.pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;

import architecture.commons.files.GraphFileWriter;

public class PackageGraphFileWriter extends GraphFileWriter<ValueGraph<String, Integer>> {

	public PackageGraphFileWriter(File output) throws FileNotFoundException {
		super(output);
	}

	@Override
	public void write(ValueGraph<String, Integer> input) {		
		Set<EndpointPair<String>> edges = input.edges();
		for(EndpointPair<String> pair : edges) {
			String from = pair.source();
			String to = pair.target();
			String type = input.edgeValueOrDefault(pair.source(), pair.target(), 0).toString();
			
			appendLine(from, to, type);
		}

		writer.close();
	}

}
