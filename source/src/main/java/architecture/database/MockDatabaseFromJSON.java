package architecture.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class MockDatabaseFromJSON extends AbstractDatabase {
	
	public MockDatabaseFromJSON(String project) {
		Gson gson = new Gson();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(FilenameUtils.getBaseName(project) + ".json"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		Type type = new TypeToken<MySQLDatabase>(){}.getType();
		AbstractDatabase intermediate = gson.fromJson(br, type);
		
		commits = intermediate.commits;
		statuses = intermediate.statuses;
		jobIDs = intermediate.jobIDs;
		
	}

}
