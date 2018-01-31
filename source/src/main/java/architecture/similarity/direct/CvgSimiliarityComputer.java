package architecture.similarity.direct;

import java.io.File;
import java.util.Map;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class CvgSimiliarityComputer extends DirectSimiliarityComputer {

	@Override
	public Map<String, Double> computeSimilarity(File arcOne, File arcTwo) {
		double[] cov = callPythonCalc(arcOne, arcTwo);
		
		metrics.put("cvgSource", cov[0]);
		metrics.put("cvgTarget", cov[1]);
		
		
		return metrics;
	}
	
	private static synchronized double[] callPythonCalc(File arcOne, File arcTwo) {
		PythonInterpreter interpreter = new PythonInterpreter();
		interpreter.exec("import sys\nsys.path.append('arcadepy')\nimport simevolanalyzer");
		
		interpreter.set("src", arcOne.getAbsolutePath());
		interpreter.set("target", arcTwo.getAbsolutePath());
		
		interpreter.exec("(a,b) = simevolanalyzer.compareTwoVersions(src, target)");
		PyObject sourceCoverage = interpreter.get("a");
		PyObject targetCoverage = interpreter.get("b");
		
		interpreter.close();
		
		return new double[]{sourceCoverage.asDouble(), targetCoverage.asDouble()};
	}

	@Override
	public double getNormalizedDifference(double simValue) {
		return (1 - simValue);
	}

}
