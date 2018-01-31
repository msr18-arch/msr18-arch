package architecture.commons.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;

public abstract class GraphFileWriter<T> {
	protected PrintStream out;
	protected PrintWriter writer;
	
	public abstract void write(T input);
	
	public GraphFileWriter(File output) throws FileNotFoundException {
		out = new PrintStream(output);
		writer = new PrintWriter(out);
	}
	
	protected void appendLine(String from, String to, String type) {
		if(!from.isEmpty() && !to.isEmpty()) {
			writer.println(type + " " + from + " " + to);
		}
	}
}
