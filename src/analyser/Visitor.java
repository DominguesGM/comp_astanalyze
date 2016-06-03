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
	private ArrayList<String> breakNodes = new ArrayList<String>();
	private ArrayList<String> continueNodes = new ArrayList<String>();
	private ArrayList<String> returnNodes = new ArrayList<String>();
	
	private DirectedPseudograph<String, String> dataGraph;
	private HashMap<String,HashSet<String>> use;
	private HashMap<String,HashSet<String>> def;
	private HashSet<String> useTemp;
	private HashSet<String> defTemp;

	
	private int nodeCounter = 0;
	private int edgeCounter = 0;
	
	public Visitor(AST ast){
		JSONObject rootPackage = (JSONObject) ((JSONArray) ast.getTree().get("children")).get(0);
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
	
	public ArrayList<String> exploreNode(JSONObject currentNode, ArrayList<String> prevStartNodes){
		ArrayList<String> startNodeList = prevStartNodes;
		ArrayList<String> exitNodesList = new ArrayList<String>();
		ArrayList<String> argumentList = new ArrayList<String>();
		
		JSONArray currentNodeContent = (JSONArray) currentNode.get("children");
		String childStartingNode;
		String condition;
		int i = 0;
		if(currentNode.get("name").equals("Case")){
			i = 1;
			JSONObject caseNode = (JSONObject) currentNodeContent.get(0);
			if(((String) ((JSONObject) currentNodeContent.get(0)).get("name")).equals("Literal"))
				childStartingNode = newNodeName() + ": Case " + processGeneric((JSONObject) currentNodeContent.get(0));
			else
				childStartingNode = newNodeName() + ": Default";
			graph.addVertex(childStartingNode);
			for(String node : startNodeList){
				graph.addEdge(node, childStartingNode, newEdgeName());
			}
			exitNodesList.add(childStartingNode);
			
			saveDataFlow(childStartingNode);
		} else{
			if(startNodeList != null)
				exitNodesList.addAll(startNodeList);
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
					argumentList.clear();
					argumentList.add(childStartingNode);
					exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), argumentList));
					// if second block exists, process
					if(((JSONArray) newNode.get("children")).size() > 2){
						// Process Else (second block)
						argumentList.clear();
						argumentList.add(childStartingNode);
						exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(2), argumentList));
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

					// reset exitNodesList array and process code block
					argumentList.clear();
					argumentList.add(childStartingNode);
					exitNodesList = exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), argumentList);

					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					exitNodesList.clear(); //fix
					
					// connect breaks and continues
					System.out.println(breakNodes);
					exitNodesList.addAll(breakNodes);
					breakNodes.clear();
					for(String node : continueNodes){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					continueNodes.clear();
					
					// the conditional node is were the loop will end and connect to the rest of the code
					exitNodesList.add(childStartingNode);
					break;
				case "For":
					// process condition and create condition node
					String assignment = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					String assignmentNode = newNodeName() + ": " + assignment;
					graph.addVertex(assignmentNode);
					
					//Dataflow related
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
					
					// Explore For code block
					argumentList.clear();
					argumentList.add(conditionNode);
					exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(3), argumentList));

					//Dataflow related
					saveDataFlow(conditionNode);
					
					// process condition and create condition node
					String statement = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(2));
					String statementNode = newNodeName() + ": " + statement;
					graph.addVertex(statementNode);
					

					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, statementNode, newEdgeName());
					}
					graph.addEdge(statementNode, conditionNode, newEdgeName());
					
					// reset exitNodesList array
					exitNodesList.clear();
					
					exitNodesList.addAll(breakNodes);
					breakNodes.clear();
					for(String node : continueNodes){
						graph.addEdge(node, conditionNode, newEdgeName());
					}
					continueNodes.clear();
					
					// the conditional node is were the loop will end and connect to the rest of the code
					exitNodesList.add(conditionNode);
					
					//Dataflow related
					saveDataFlow(statementNode);
					break;
				case "Do":
					// process condition and create condition node
					condition = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					conditionNode = newNodeName() + ": " + condition;
					String doStartNode = newNodeName() + ": do";
					graph.addVertex(conditionNode);
					graph.addVertex(doStartNode);
					
					saveDataFlow(conditionNode);
					
					// connect DO node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, doStartNode, newEdgeName());
					}

					// reset exitNodesList array and process code block
					argumentList.clear();
					argumentList.add(doStartNode);
					exitNodesList = exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), argumentList);

					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, conditionNode, newEdgeName());
					}
					exitNodesList.clear(); //fix
					
					graph.addEdge(conditionNode, doStartNode, newEdgeName());
					
					// connect breaks and continues
					System.out.println(breakNodes);
					exitNodesList.addAll(breakNodes);
					breakNodes.clear();
					for(String node : continueNodes){
						graph.addEdge(node, doStartNode, newEdgeName());
					}
					continueNodes.clear();
					
					// the conditional node is were the loop will end and connect to the rest of the code
					exitNodesList.add(conditionNode);
					break;
				case "Switch":
					// process condition and create condition node
					condition = processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					childStartingNode = newNodeName() + ": switch(" + condition + ")";
					graph.addVertex(childStartingNode);
					
					saveDataFlow(childStartingNode);
					
					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					
					// reset exitNodesList array
					exitNodesList.clear();
					
					// if there's no break statement in any case, it'll fall through to the next Case's first statement
					ArrayList<String> lastCaseElement = new ArrayList<String>();
					
					for(int j = 1; j < ((JSONArray) newNode.get("children")).size(); j++){
						argumentList.clear();
						lastCaseElement.clear();
						argumentList.add(childStartingNode);
						argumentList.addAll(lastCaseElement);
						lastCaseElement.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(j), argumentList));
					}
					
					exitNodesList.addAll(lastCaseElement);
					exitNodesList.addAll(breakNodes);
					breakNodes.clear();
					
					break;
				case "Return":
					childStartingNode = newNodeName()+": return";
					graph.addVertex(childStartingNode);
					returnNodes.add(childStartingNode);
					
					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					
					// reset exitNodesList array
					exitNodesList.clear();
					return exitNodesList;
				case "Continue":
					childStartingNode = newNodeName()+": continue";
					graph.addVertex(childStartingNode);
					continueNodes.add(childStartingNode);
					
					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					
					// reset exitNodesList array
					exitNodesList.clear();
					return exitNodesList;
				case "Break":
					childStartingNode = newNodeName()+": break";
					graph.addVertex(childStartingNode);
					breakNodes.add(childStartingNode);
					
					// connect condition node to previous child end nodes
					for(String node : exitNodesList){
						graph.addEdge(node, childStartingNode, newEdgeName());
					}
					
					// reset exitNodesList array
					exitNodesList.clear();
					return exitNodesList;
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
		case "OperatorAssignment":
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
		default:
			return type;
		}
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

