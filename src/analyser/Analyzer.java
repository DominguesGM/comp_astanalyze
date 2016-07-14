package analyser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import data.AST;

public class Analyzer {	
	private JSONObject rootPackage;
	
	public Analyzer(AST ast){
		rootPackage = (JSONObject) ((JSONArray) ast.getTree().get("children")).get(0);
	}
	
	public void analyze(){
		for(int i = 0; i < ((JSONArray) rootPackage.get("children")).size(); i++){
			PackageAnalyzer packageAnalyzer = new PackageAnalyzer((JSONObject) ((JSONArray) rootPackage.get("children")).get(i));
			packageAnalyzer.analyse();			
		}
	}
}

