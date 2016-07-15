package analyser;
import java.io.IOException;
import java.util.ArrayList;

import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import other.Log;
import output.Output;

public class ClassAnalyzer {
	private DirectedPseudograph<String, String> controlGraph;	
	private DirectedPseudograph<String, String> dataGraph;
		
	private int dataEdgeCounter = 0;	
	private int nodeCounter = 0;
	private int edgeCounter = 0;
	
	private JSONObject classObject;
	private String packageName;
	private String className;
	
	public ClassAnalyzer(JSONObject classObject, String packageName){
		this.classObject = classObject;
		this.packageName = packageName;
		this.className = (String) classObject.get("content");
		
		this.controlGraph = new DirectedPseudograph<String,String>(String.class);
		this.dataGraph = new DirectedPseudograph<String,String>(String.class);
	}
	
	public void analyse(){
		Log.detail("Analysing class " + className);
		
		JSONObject func;
		JSONObject funcContents;
		String funcName = "";
		
		for(int i = 0; i < ((JSONArray) classObject.get("children")).size(); i++){
			if(((JSONObject)((JSONArray) classObject.get("children")).get(i)).get("name").equals("MethodImpl")){
				func = (JSONObject) ((JSONArray) classObject.get("children")).get(i);
				funcName = (String) func.get("content");
				
				ArrayList<String> arguments = new ArrayList<String>();
				for(int j = 0; j < ((JSONArray) func.get("children")).size(); j++){
					// Retrieve function content
					funcContents = (JSONObject) ((JSONArray) func.get("children")).get(j);
					// If content is the functions code block
					if(j+1 == ((JSONArray) func.get("children")).size()){
						new FunctionAnalyzer(this, funcName, arguments, funcContents, controlGraph, dataGraph);
					} else {
						if(j == 0){
							arguments.add((String) funcContents.get("content"));
						} else {
							//If content is a parameter, parse parameter and insert into arguments list
							String parameter = (String) ((JSONObject)((JSONArray)funcContents.get("children")).get(0)).get("content");
							arguments.add(parameter);
							parameter =(String) funcContents.get("content");
							arguments.add(parameter);
						}
					}
				}
			}
		}
	}

	public String newNodeName(){
		String newNode = Integer.toString(nodeCounter);
		nodeCounter++;
		return newNode;
	}
	
	public String newEdgeName(){
		String newEdge = Integer.toString(edgeCounter);
		edgeCounter++;
		return newEdge;
	}
	
	public String newDataEdgeName(){
		String newEdge = Integer.toString(dataEdgeCounter);
		dataEdgeCounter++;
		return newEdge;
	}
	
	public DirectedPseudograph<String,String> getControlGraph(){
		return controlGraph;
	}
	
	public DirectedPseudograph<String,String> getDataGraph(){
		return dataGraph;
	}

	public void export(){
		Output output = new Output(this, "export/"+packageName);
		
		try {
			output.printControlGraph("export/" + packageName + "/" + className + "_control.dot");
			output.printDataGraph("export/"+ packageName + "/" + className + "_data.dot");
			Log.info("export/" + packageName + "/" + className + "_control.dot created.");
			Log.info("export/"+ packageName + "/" + className + "_data.dot created.");
		} catch (IOException e) {
			Log.error("Error outputing dot files");
			return;
		}
	}
}

