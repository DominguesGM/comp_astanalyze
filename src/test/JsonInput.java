package test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JsonInput {
	
	public static void main(String args[]){
		FileReader freader = null;
		try {
			freader = new FileReader("json\\ast.json");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		int read = 0;
		String jsonString = "";
		try {
			while((read = freader.read()) != -1){
				jsonString += (char) read;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject json = new JSONObject();
		System.out.println(json);
		json = (JSONObject) JSONValue.parse(jsonString);
		System.out.println(json);
		
		System.out.println(json.get("children"));
	}
	
}
