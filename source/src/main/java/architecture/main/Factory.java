package architecture.main;

import architecture.database.AbstractDatabase;
import architecture.database.MockDatabaseFromJSON;
import architecture.database.MySQLDatabase;
import architecture.extraction.AbstractArchitectureExtractor;
import architecture.extraction.MultiSplittedArchitectureExtractor;
import architecture.extraction.classes.AbstractClassGraphExtractor;
import architecture.extraction.classes.husacct.HusacctGraphExtractor;
import architecture.extraction.reconstruction.ACDCReconstructor;
import architecture.extraction.reconstruction.AbstractArchitectureReconstructor;
import architecture.extraction.reconstruction.pkg.PackageReconstructor;
import architecture.similarity.AbstractArchitectureSimilarityComputer;
import architecture.similarity.direct.A2aSimiliarityComputer;
import architecture.similarity.direct.CvgSimiliarityComputer;
import architecture.similarity.pairwise.graph.GraphSimiliarityComputer;

public class Factory {
	
	public final static String ACDC_FILE_BASE = "acdcArc";
	public final static String PKG_FILE_BASE = "pkgArc";
	
	private static CompilableList compListInstance;
	
	public static AbstractArchitectureExtractor createExtractor() {
		AbstractClassGraphExtractor extractor = new HusacctGraphExtractor(false);
		AbstractArchitectureReconstructor reconstructorAcdc = new ACDCReconstructor(ACDC_FILE_BASE);
		AbstractArchitectureReconstructor reconstructorPkg = new PackageReconstructor(PKG_FILE_BASE);
		//return new SplittedArchitectureExtractor(extractor, reconstructor, "husacct_simple");
		return new MultiSplittedArchitectureExtractor(extractor, 
				new AbstractArchitectureReconstructor[]{reconstructorAcdc, reconstructorPkg}, 
				"husacct");
	}
	
	public static AbstractDatabase createDatabase(String projectName) {
		return new MockDatabaseFromJSON(projectName);
		//return new MySQLDatabase(projectName);
	}
	
	public static AbstractDatabase createDatabaseFile(String projectName) {
		return new MySQLDatabase(projectName);
	}
	
	public static AbstractArchitectureSimilarityComputer createSimilarityComputer() {
		return new CvgSimiliarityComputer();
	}
	
	public static AbstractArchitectureSimilarityComputer createA2aComputer() {
		return new A2aSimiliarityComputer();
	}
	
	public static AbstractArchitectureSimilarityComputer createCvgComputer() {
		return new CvgSimiliarityComputer();
	}
	
	public static AbstractArchitectureSimilarityComputer createPkgComputer() {
		return new GraphSimiliarityComputer();
	}
	
	public static CommitToArchitecture createCommitToArchitecture(
			String downloadFolder, String arcFolder, String compPath, String project) {
		return new CommitToArchitecture(downloadFolder, arcFolder, project);
	}
	
	public static BuildResultAnalyzer createBuildResultAnalyzer(String downloadFolder, String project) {
		return new BuildResultAnalyzer(downloadFolder, project);
	}
	
	public synchronized static CompilableList createCompilableList(String path) {
		if(compListInstance == null) {
			compListInstance = new CompilableList(path);
		}
		return compListInstance;
	}
	
	public static CompareAndSave createCompareAndSave(String path) {
		return new CompareAndSave(path);
	}
}
