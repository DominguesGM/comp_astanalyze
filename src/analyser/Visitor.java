package analyser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import data.AST;

public class Visitor {
	private JSONObject mainFunction;
	
	private DirectedPseudograph<String, String> graph;
	private DirectedPseudograph<String, String> dataGraph;
	
	private HashMap<String,HashSet<String>> use;
	private HashMap<String,HashSet<String>> def;

	private HashSet<String> useTemp;
	private HashSet<String> defTemp;

	
	private int nodeCounter = 0;
	private int edgeCounter = 0;
	
	public Visitor(AST ast){
		JSONObject rootPackage = (JSONObject) ((JSONArray) AnalyseAst.getAST().getTree().get("children")).get(0);
		JSONObject packageMain = (JSONObject) ((JSONArray) rootPackage.get("children")).get(0);
		JSONObject mainClass = (JSONObject) ((JSONArray) packageMain.get("children")).get(0);
		JSONObject mainFunc = (JSONObject) ((JSONArray) mainClass.get("children")).get(1);
		JSONObject mainFuncCode = (JSONObject) ((JSONArray) mainFunc.get("children")).get(2);
	
		graph = new DirectedPseudograph<String,String>(String.class);
		dataGraph = new DirectedPseudograph<String,String>(String.class);
		
		use = new HashMap<>();
		def = new HashMap<>();

		useTemp = new HashSet<>();
		defTemp = new HashSet<>();
		
		exploreNode(mainFuncCode, null);
		
		System.out.println("DEF " + def);
		
		System.out.println("USE " + use);
	}
	
	public ArrayList<String> exploreNode(JSONObject currentNode, String prevStartNode){
		String startNode = prevStartNode;
		ArrayList<String> exitNodesList = new ArrayList<String>();
		
		JSONArray currentNodeContent = (JSONArray) currentNode.get("children");
		String childStartingNode;
		String condition;
		int i = 0;
		if(currentNode.get("name").equals("Case")){
			i = 1;
			JSONObject caseNode = (JSONObject) currentNodeContent.get(0);
			childStartingNode = newNodeName() + ": Case " + processGeneric((JSONObject) currentNodeContent.get(0));
			graph.addVertex(childStartingNode);
			graph.addEdge(startNode, childStartingNode, newEdgeName());
			exitNodesList.add(childStartingNode);
			
			saveDataFlow(childStartingNode);
		} else{
			if(startNode != null)
				exitNodesList.add(startNode);
		}
		for(; i < currentNodeContent.size(); i++){
			condition = null;
			JSONObject newNode = (JSONObject) currentNodeContent.get(i);
			System.out.println(newNode.get("name"));
			
			switch((String) newNode.get("name")){
				case "If":
					// process condition and create condition node
					condition = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					childStartingNode = newNodeName() + ": " + condition;
					System.out.println(childStartingNode);
					graph.addVertex(childStartingNode);
					
					saveDataFlow(childStartingNode);
					
					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					
					// reset exitNodesList array
					exitNodesList.clear();
					
					// process first block
					exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), childStartingNode));
					// if second block exists, process
					if(((JSONArray) newNode.get("children")).size() > 2){
						// Process Else (second block)
						exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(2), childStartingNode));
					}
					break;
				case "While":
					// process condition and create condition node
					condition = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					childStartingNode = newNodeName() + ": " + condition;
					System.out.println(childStartingNode);
					graph.addVertex(childStartingNode);
					
					saveDataFlow(childStartingNode);
					
					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					
					// reset exitNodesList array
					exitNodesList.clear();
					
					// process code block
					exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), childStartingNode));

					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					
					// the conditional node is were the loop will end and connect to the rest of the code
					exitNodesList.add(childStartingNode);
					break;
				case "For":
					// process condition and create condition node
					String assignment = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					String assignmentNode = newNodeName() + ": " + assignment;
					graph.addVertex(assignmentNode);
					
					saveDataFlow(assignmentNode);
					
					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, assignmentNode, newEdgeName());
					}
					
					// reset exitNodesList array
					exitNodesList.clear();
					
					// process condition and create condition node
					condition = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(1));
					String conditionNode = newNodeName() + ": " + condition;
					graph.addVertex(conditionNode);
					graph.addEdge(assignmentNode, conditionNode, newEdgeName());
					
					saveDataFlow(conditionNode);
					
					// process condition and create condition node
					String statement = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(2));
					String statementNode = newNodeName() + ": " + statement;
					graph.addVertex(statementNode);
					graph.addEdge(conditionNode, statementNode, newEdgeName());
					
					saveDataFlow(statementNode);
					
					// Do something for for node
					exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), statementNode));
					break;
				case "Switch":
					// process condition and create condition node
					condition = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					childStartingNode = newNodeName() + ": " + condition;
					graph.addVertex(childStartingNode);
					
					saveDataFlow(childStartingNode);
					
					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					
					// reset exitNodesList array
					exitNodesList.clear();
					
					for(int j = 1; j < ((JSONArray) newNode.get("children")).size(); j++){
						exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), childStartingNode));
					}
					break;
				case "Return":
					break;
				default:
					// process statement and create condition node
					childStartingNode = newNodeName() + ": " + processGeneric(newNode);
					System.out.println(childStartingNode);
					graph.addVertex(childStartingNode);
					
					saveDataFlow(childStartingNode);
					
					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					
					exitNodesList.clear();
					exitNodesList.add(childStartingNode);
					break;
			}
		}
		
		return exitNodesList;
	}

	private void saveDataFlow(String nodeId) {
		use.put(nodeId, (HashSet<String>) useTemp.clone());
		useTemp.clear();
		def.put(nodeId, (HashSet<String>) defTemp.clone());
		defTemp.clear();
	}

	private String processGeneric(JSONObject node) {
		String type = (String) node.get("name");
		String content = (String) node.get("content");
		JSONArray children = (JSONArray) node.get("children");
		String leftSide = null;
		String rightSide = null;
		String output = null;
		
		switch(type){
		case "TypeReference":
			return content;
		case "VariableRead":
			output = processGeneric((JSONObject) children.get(1));
			
			// mark variable use
			useTemp.add(output);
			
			return output;
		case "LocalVariable":
			output = processGeneric((JSONObject) children.get(0)) + " " + content;
			if(children.size() == 2){
				output += " = " + processGeneric((JSONObject) children.get(1));
			}

			// mark variable definition
			defTemp.add(content);
			return output;
		case "Literal":
			return content;
		case "LocalVariableReference":
			return content;
		case "BinaryOperator":
			rightSide = processGeneric((JSONObject)children.get(2));
			leftSide = processGeneric((JSONObject)children.get(1));
			return leftSide + content + rightSide;
		case "Assignment":
			leftSide = processGeneric((JSONObject)children.get(1));			
			rightSide = processGeneric((JSONObject)children.get(2));
			return leftSide + " = " + rightSide;
		case "VariableWrite":
			output = processGeneric((JSONObject)children.get(1));
			
			// mark variable definition
			defTemp.add(output);
			
			return output;
		case "OperatorAssignement":
			return processGeneric((JSONObject)children.get(2)) +" "+ content +" "+ processGeneric((JSONObject)children.get(1));
		case "UnaryOperator":
			if(content.charAt(0) == '_'){
				String contentEdited = content.replace("_", "");
				String variable = processGeneric((JSONObject)children.get(1));
				defTemp.add(variable);
				return variable + contentEdited;
			} else {
				String contentEdited = content.replace("_", "");
				String variable = processGeneric((JSONObject)children.get(1));
				defTemp.add(variable);
				return contentEdited + variable;
			}
		case "Break":
			return type;
		case "Continue":
			return type;
		case "Return":
			return type;
		}
		
		return null;
	}
	
	private String newNodeName(){
		String newNode = Integer.toString(nodeCounter);
		nodeCounter++;
		return newNode;
	}
	
	private String newEdgeName(){
		String newEdge = Integer.toString(edgeCounter);
		edgeCounter++;
		return newEdge;
	}
	
	public DirectedPseudograph<String,String> getGraph(){
		return graph;
	}
}

