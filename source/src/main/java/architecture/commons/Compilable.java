package architecture.commons;

public class Compilable {
	private String commitID;
	private int compilable;
	
	public Compilable(String commitID, int isCompilable) {
		this.commitID = commitID;
		this.compilable = isCompilable;
	}
	
	public String getCommitID() {
		return commitID;
	}
	public void setCommitID(String commitID) {
		this.commitID = commitID;
	}
	public int getCompilable() {
		return compilable;
	}
	public void setCompilable(int compilable) {
		this.compilable = compilable;
	}
}
