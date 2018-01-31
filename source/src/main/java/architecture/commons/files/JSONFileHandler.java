package architecture.commons.files;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JSONFileHandler<T> {
	
	public List<T> readJson(final Class<T> clazz, String path) throws IOException {
		return fromJsonList(path, clazz);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> List<T> fromJsonList(String path, Class<T> clazz) throws FileNotFoundException {
		Gson gson = new Gson();
		BufferedReader br = new BufferedReader(new FileReader(path));
		Object[] array = (Object[])java.lang.reflect.Array.newInstance(clazz, 1);
	    array = gson.fromJson(br, array.getClass());
	    List<T> list = new ArrayList<T>();
	    for (int i=0 ; i<array.length ; i++)
	        list.add((T)array[i]);
	    return list; 
	}
	
	public void writeJson(String path, List<T> diffList) throws IOException {
		PrintStream out = new PrintStream(path);;
		PrintWriter writer = new PrintWriter(out);
		
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();	    
	    Type type = new TypeToken<List<T>>(){}.getType();

		writer.println(gson.toJson(diffList, type));
		writer.close();
	}
	
}
