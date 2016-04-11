package data;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import other.Log;

public class AST {
	private JSONObject ast;
	
	public AST(){
		ast = null;
	}
	
	public AST(String astFile) {
		try{
			parseFile(astFile);
		} catch(IOException e){
			ast = null;
			Log.error("Could not open file to read AST from");
			e.printStackTrace();
		}
	}
	
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
	
	
	public JSONObject getTree(){
		return ast;
	}
	
}
