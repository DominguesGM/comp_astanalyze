package analyser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import data.AST;
import javafx.util.Pair;

public class Visitor {
	private JSONObject mainFunction;
	
	private DirectedPseudograph<String, String> graph;
	
	private DirectedPseudograph<String, String> dataGraph;
	
	
	private FunctionAnalyzer functionAnalyzer;

	private int dataEdgeCounter = 0;	
	private int nodeCounter = 0;
	private int edgeCounter = 0;

	
	public Visitor(AST ast){
		JSONObject rootPackage = (JSONObject) ((JSONArray) ast.getTree().get("children")).get(0);
		JSONObject packageMain = (JSONObject) ((JSONArray) rootPackage.get("children")).get(0);
		JSONObject mainClass = (JSONObject) ((JSONArray) packageMain.get("children")).get(0);
		JSONObject func;
		JSONObject funcContents;
	
		graph = new DirectedPseudograph<String,String>(String.class);
		dataGraph = new DirectedPseudograph<String,String>(String.class);
		
		
		
		String funcName = "";
		for(int i = 1; i < ((JSONArray) mainClass.get("children")).size(); i++){
			func = (JSONObject) ((JSONArray) mainClass.get("children")).get(i);
			funcName = (String) func.get("content");
			
			ArrayList<String> arguments = new ArrayList<String>();
			for(int j = 0; j < ((JSONArray) func.get("children")).size(); j++){
				// Retrieve function content
				funcContents = (JSONObject) ((JSONArray) func.get("children")).get(j);
				// If content is the functions code block
				if(j+1 == ((JSONArray) func.get("children")).size()){
					functionAnalyzer = new FunctionAnalyzer(this, funcName, arguments, funcContents, graph, dataGraph);
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
		return graph;
	}
	
	public DirectedPseudograph<String,String> getDataGraph(){
		return dataGraph;
	}
	
}

