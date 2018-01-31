package architecture.extraction.classes.husacct;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

import architecture.commons.files.GraphFileWriter;

public class HusaactGraphFileWriter extends GraphFileWriter<Element> {

	public static final String SUB_PKG = "subPkg";
	public static final String CONTAINS = "contains";
	public static final String REFERENCES = "references";
	
	private boolean simple;

	public HusaactGraphFileWriter(File output, boolean simple) throws FileNotFoundException {
		super(output);
		
		this.simple = simple;
	}

	public void write(Element input) {

		List<Content> packages = ((Element) input.getContent(1)).getContent();
		List<Content> classes = ((Element) input.getContent(2)).getContent();
		List<Content> dependencies = ((Element) input.getContent(4)).getContent();

		handlePackages(packages);
		handleClasses(classes);
		handleDependencies(dependencies);
		
		writer.close();
	}
	
	private boolean containsInvalid(String element) {
		return element.contains("xLibraries") || element.contains("No_Package");
	}

	@Override
	protected void appendLine(String from, String to, String type) {
		if(simple) {
			type = "depends";
		}
		if(!containsInvalid(from) && !containsInvalid(to)) {
			super.appendLine(from, to, type);
			return;
		}
		
		if(!from.isEmpty() && !to.isEmpty() &&
				!containsInvalid(from) && !containsInvalid(to)) {
			if(simple) {
				type = "depends";
			}
			writer.println(type + " " + from + " " + to);
		}

	}

	private void handlePackages(List<Content> packages) {
		for (Content c : packages) {
			Element cElement = (Element) c;
			String fullPkgName = cElement.getContent().get(1).getValue();
			String parentFullName = cElement.getContent().get(4).getValue();
			
			appendLine(parentFullName, fullPkgName, SUB_PKG);
		}
	}

	private void handleClasses(List<Content> classes) {
		for (Content c : classes) {
			Element cElement = (Element) c;
			String simpleName = cElement.getContent().get(0).getValue();
			String pkg = cElement.getContent().get(4).getValue();

			String fullName = pkg + "." + simpleName;

			appendLine(pkg, fullName, CONTAINS);
		}
	}

	private void handleDependencies(List<Content> dependencies) {
		for (Content c : dependencies) {
			Element cElement = (Element) c;
			String fromType = cElement.getContent().get(0).getValue();
			String toType = cElement.getContent().get(1).getValue();
			
			appendLine(fromType, toType, REFERENCES);
		}
	}

}
