package architecture.commons;

import java.util.Map;

public class VersionDifference {
	private int fromVersion;
	private int toVersion;
	private Map<String, Map<String, Double>> metrics;
	
	public VersionDifference(int from, int to, Map<String, Map<String, Double>> metrics) {
		this.fromVersion = from;
		this.toVersion = to;
		this.metrics = metrics;
	}
	
	public Map<String, Map<String, Double>> getMetrics() {
		return metrics;
	}

	public void setMetrics(Map<String, Map<String, Double>> metrics) {
		this.metrics = metrics;
	}

	public int getFromVersion() {
		return fromVersion;
	}
	
	public void setFromVersion(int fromVersion) {
		this.fromVersion = fromVersion;
	}
	
	public int getToVersion() {
		return toVersion;
	}
	
	public void setToVersion(int toVersion) {
		this.toVersion = toVersion;
	}

	
}
