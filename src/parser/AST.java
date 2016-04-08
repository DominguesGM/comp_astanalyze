package parser;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class AST {
	private JSONObject ast;
	
	/**
	 * parseFile.
	 * Provided a file name of a json text file, generates the JSONObject.
	 * @param astFile
	 * @throws IOException
	 */
	public void parseFile(String astFile) throws IOException{
		FileReader freader = null;
		freader = new FileReader(astFile);
		int read = 0;
		String jsonString = "";
		while((read = freader.read()) != -1){
			jsonString += (char) read;
		}
		ast = (JSONObject) JSONValue.parse(jsonString);
	}
	
	
}
