package architecture.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractDatabase {
	
	
	protected Map<Integer, String> commits;
	protected Map<Integer, String> statuses;
	protected Map<Integer, String> jobIDs;
		
	public List<Integer> getBuildList() {
		Set<Integer> commitSet = commits.keySet();
		
		List<Integer> res = new ArrayList<Integer>();
		res.addAll(commitSet);
		Collections.sort(res);
		
		return res;
	}
	
	public String getCommit(int build) {
		return commits.get(build);
	}
	
	public String getOutcome(int build) {
		return statuses.get(build);
	}
	
	public String getJobID(int build) {
		return jobIDs.get(build);
	}
	
}
