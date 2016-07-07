package analyser;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import other.Log;

public class PackageAnalyzer {	
	private JSONObject packageObject;
	private String packageName; 

	public PackageAnalyzer(JSONObject packageObject){
		this.packageObject = packageObject;
		this.packageName = (String) packageObject.get("content");
		createPackageFolder();
	}

	private void createPackageFolder(){
		try{
			File exportDir = new File("export/" + packageName);
			exportDir.mkdir();
		}catch(Exception e){
			Log.error("Error creating output folder.");
		}
	}

	public void analyse(){	
		Log.detail("Analysing package " + packageName);
		
		for(int j = 0; j < ((JSONArray) packageObject.get("children")).size(); j++){
			// analyze class
			JSONObject classObject = (JSONObject) ((JSONArray) packageObject.get("children")).get(j);

			ClassAnalyzer classAnalysis;

			try{
				classAnalysis = new ClassAnalyzer(classObject, packageName);
				classAnalysis.analyse();
			} catch(Exception e){
				Log.error("Error while analyzing the abstract syntax tree. Check json format.");
				e.printStackTrace();
				return;
			}
			
			classAnalysis.export();
		}
	}
}