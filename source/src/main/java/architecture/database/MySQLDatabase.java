package architecture.database;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MySQLDatabase extends AbstractDatabase {
	
	private String url = "jdbc:mysql://localhost:3306/archi?useSSL=false&serverTimezone=UTC";
	private String username = "archi";
	private String password = "archi";
	
	private String query;
	
	public MySQLDatabase(String project) {
		commits = new HashMap<Integer, String>();
		statuses = new HashMap<Integer, String>();
		jobIDs = new HashMap<Integer, String>();
		
		 query = 
					"select distinct tr_log_analyzer, gh_project_name, git_trigger_commit, tr_build_number, tr_job_id, tr_status "
					+ "from travistorrent_8_2_2017 "
					+ "where gh_project_name = \""+ project +"\" "
					+ "&& tr_log_analyzer = \"java-maven\" "
					+ "&& git_branch = \"master\" "
					+ "order by tr_build_number ;";

		
		
		getData();
		
		try {
			writeJson(FilenameUtils.getBaseName(project) + ".json");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void getData() {
		try (Connection connection = DriverManager.getConnection(url, username, password)) {
			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			while(rs.next()) {
				String commit = rs.getString("git_trigger_commit");
				int number = rs.getInt("tr_build_number");
				String status = rs.getString("tr_status");
				String jobID = rs.getString("tr_job_id");
				
				if(commits.containsValue(commit)) {
					continue;
				}
				
				commits.put(number, commit);
				statuses.put(number, status);
				jobIDs.put(number, jobID);
				
			}
			
		} catch (SQLException e) {
		    throw new IllegalStateException("Cannot connect the database!", e);
		}
	}
	
	public void writeJson(String path) throws IOException {
		PrintStream out = new PrintStream(path);;
		PrintWriter writer = new PrintWriter(out);
		
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();	    
	    Type type = new TypeToken<AbstractDatabase>(){}.getType();

		writer.println(gson.toJson(this, type));
		writer.close();
	}

}
