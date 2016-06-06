package analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CodeProcessor {
	
	private DirectedPseudograph<String, String> graph;
	private ArrayList<String> breakNodes;
	private ArrayList<String> continueNodes;
	private ArrayList<String> returnNodes;
	
	private HashSet<String> useTemp;
	private HashSet<String> defTemp;
	
	private ArrayList<DataDependency> dataDependency;
	private HashMap<String,HashSet<String>> use;
	private HashMap<String,HashSet<String>> def;
	private boolean exitLoopControl = false;
	
	private CodeGenerator generator;
	private Visitor parent;
	
	
	public CodeProcessor(Visitor parent, String funcName, ArrayList<String> arguments, JSONObject funcCode, DirectedPseudograph<String, String> graph, 
			ArrayList<DataDependency> dataDependency,
			HashMap<String,HashSet<String>> use,
			HashMap<String,HashSet<String>> def){
		this.parent = parent;
		this.graph = graph;
		this.breakNodes = new ArrayList<String>();
		this.continueNodes = new ArrayList<String>();
		this.returnNodes = new ArrayList<String>();
		
		useTemp = new HashSet<>();
		defTemp = new HashSet<>();
		
		this.dataDependency = dataDependency;
		this.use = use;
		this.def = def;
		
		this.generator = new CodeGenerator(useTemp, defTemp);
		
		ArrayList<String> functionName = new ArrayList<String>();
		String tempFuncName = parent.newNodeName()+": "+arguments.get(0)+ " " +funcName+"(";
		
		for(int i = 2; i < arguments.size(); i+=2){
			tempFuncName += arguments.get(i-1) + " " +arguments.get(i);
			defTemp.add(arguments.get(i));
			if(i + 1 < arguments.size()){
				tempFuncName += ", ";
			} else{
				tempFuncName += ")";
			}
		}
		
		functionName.add(tempFuncName);
		graph.addVertex(tempFuncName);
		this.saveDataFlow(tempFuncName);
		ArrayList<String> finalNodes = exploreNode(funcCode, functionName);
		finalNodes.addAll(returnNodes);
		String exitNodeName = parent.newNodeName() + ": exit";
		graph.addVertex(exitNodeName);
		
		for(String node : finalNodes){
			graph.addEdge(node, exitNodeName, parent.newEdgeName());
		}
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
				JSONObject caseNode = (JSONObject) currentNodeContent.get(0);
				if(((String) ((JSONObject) currentNodeContent.get(0)).get("name")).equals("Literal")){
					childStartingNode = this.parent.newNodeName() + ": Case " + generator.processGeneric((JSONObject) currentNodeContent.get(0));
					i = 1;
				}else{
					childStartingNode = this.parent.newNodeName() + ": Default";
				}
				graph.addVertex(childStartingNode);
				for(String node : startNodeList){
					graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
				}
				exitNodesList.add(childStartingNode);
				
				saveDataFlow(childStartingNode);
			} else{
				if(startNodeList != null)
					exitNodesList.addAll(startNodeList);
			}
			if(currentNode.get("name").equals("Case") || currentNode.get("name").equals("Block")){
				for(; i < currentNodeContent.size(); i++){
					condition = null;
					JSONObject newNode = (JSONObject) currentNodeContent.get(i);
					exitNodesList = processNode(newNode, exitNodesList, argumentList);
					if(getExitLoopControl()){
						break;
					}
				}
			} else {
				exitNodesList = processNode(currentNode, exitNodesList, argumentList);
			}
			return exitNodesList;
	}
	
	
	public ArrayList<String> processNode(JSONObject newNode, ArrayList<String> exitNodesListParam, ArrayList<String> argumentList){
		ArrayList<String> exitNodesList = new ArrayList<String>(exitNodesListParam);
		String childStartingNode;
		String condition = "";
		switch((String) newNode.get("name")){
		case "If":
			// process condition and create condition node
			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			childStartingNode = this.parent.newNodeName() + ": if(" + condition + ")";
			graph.addVertex(childStartingNode);
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
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
			} else {
				exitNodesList.add(childStartingNode);
			}
			break;
		case "While":
			// process condition and create condition node
			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			childStartingNode = this.parent.newNodeName() + ": while(" + condition + ")";
			graph.addVertex(childStartingNode);
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}

			// reset exitNodesList array and process code block
			argumentList.clear();
			argumentList.add(childStartingNode);
			exitNodesList = exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), argumentList);

			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}
			exitNodesList.clear(); //fix
			
			// connect breaks and continues
			exitNodesList.addAll(breakNodes);
			breakNodes.clear();
			for(String node : continueNodes){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}
			continueNodes.clear();
			
			// the conditional node is were the loop will end and connect to the rest of the code
			exitNodesList.add(childStartingNode);
			setExitLoopControl(false);
			break;
		case "For":
			// process condition and create condition node
			String assignment = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			String assignmentNode = this.parent.newNodeName() + ": " + assignment;
			graph.addVertex(assignmentNode);
			
			//Dataflow related
			saveDataFlow(assignmentNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, assignmentNode, this.parent.newEdgeName());
			}
			
			// reset exitNodesList array
			exitNodesList.clear();
			
			// process condition and create condition node
			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(1));
			String conditionNode = this.parent.newNodeName() + ": " + condition;
			graph.addVertex(conditionNode);
			graph.addEdge(assignmentNode, conditionNode, this.parent.newEdgeName());
			
			//Dataflow related
			saveDataFlow(conditionNode);
			
			// Explore For code block
			argumentList.clear();
			argumentList.add(conditionNode);
			exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(3), argumentList));

			
			
			// process condition and create condition node
			String statement = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(2));
			String statementNode = this.parent.newNodeName() + ": " + statement;
			graph.addVertex(statementNode);
			

			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, statementNode, this.parent.newEdgeName());
			}
			graph.addEdge(statementNode, conditionNode, this.parent.newEdgeName());
			
			// reset exitNodesList array
			exitNodesList.clear();
			
			exitNodesList.addAll(breakNodes);
			breakNodes.clear();
			for(String node : continueNodes){
				graph.addEdge(node, statementNode, this.parent.newEdgeName());
			}
			continueNodes.clear();
			
			// the conditional node is were the loop will end and connect to the rest of the code
			exitNodesList.add(conditionNode);
			
			//Dataflow related
			saveDataFlow(statementNode);
			setExitLoopControl(false);
			break;
		case "Do":
			String doStartNode = this.parent.newNodeName() + ": do";
			// process condition and create condition node
			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			conditionNode = this.parent.newNodeName() + ": while(" + condition+");";
			
			graph.addVertex(conditionNode);
			graph.addVertex(doStartNode);
			
			saveDataFlow(conditionNode);
			saveDataFlow(doStartNode);
			
			// connect DO node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, doStartNode, this.parent.newEdgeName());
			}

			// reset exitNodesList array and process code block
			argumentList.clear();
			argumentList.add(doStartNode);
			exitNodesList = exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), argumentList);

			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, conditionNode, this.parent.newEdgeName());
			}
			exitNodesList.clear(); //fix
			
			graph.addEdge(conditionNode, doStartNode, this.parent.newEdgeName());
			
			// connect breaks and continues
			exitNodesList.addAll(breakNodes);
			breakNodes.clear();
			for(String node : continueNodes){
				graph.addEdge(node, doStartNode, this.parent.newEdgeName());
			}
			continueNodes.clear();
			
			// the conditional node is were the loop will end and connect to the rest of the code
			exitNodesList.add(conditionNode);
			setExitLoopControl(false);
			break;
		case "Switch":
			// process condition and create condition node
			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			childStartingNode = this.parent.newNodeName() + ": switch(" + condition + ")";
			graph.addVertex(childStartingNode);
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
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
			String returnContent = "";
			if(((JSONArray) newNode.get("children")).size() != 0)
				returnContent = " " + generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			childStartingNode = this.parent.newNodeName()+": return" + returnContent;
			graph.addVertex(childStartingNode);
			returnNodes.add(childStartingNode);
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}
			
			// reset exitNodesList array
			exitNodesList.clear();
			setExitLoopControl(true);
			break;
		case "Continue":
			childStartingNode = this.parent.newNodeName()+": continue";
			graph.addVertex(childStartingNode);
			continueNodes.add(childStartingNode);
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}
			
			// reset exitNodesList array
			exitNodesList.clear();
			setExitLoopControl(true);
			break;
		case "Break":
			childStartingNode = this.parent.newNodeName()+": break";
			graph.addVertex(childStartingNode);
			breakNodes.add(childStartingNode);
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}
			
			// reset exitNodesList array
			exitNodesList.clear();
			setExitLoopControl(true);
			break;
		default:
			// process statement and create condition node
			childStartingNode = this.parent.newNodeName() + ": " + generator.processGeneric(newNode);
			graph.addVertex(childStartingNode);
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}
			
			exitNodesList.clear();
			exitNodesList.add(childStartingNode);
			break;
		}
		return exitNodesList;
	}
	
	private void setExitLoopControl(boolean var){
		this.exitLoopControl = var;
	}
	
	private boolean getExitLoopControl(){
		return this.exitLoopControl ;
	}
	
	
	
	
	private void saveDataFlow(String nodeId) {
		ArrayList<String> predecessors = new ArrayList<String>(graph.incomingEdgesOf(nodeId));
		DataDependency dependencyTemp = new DataDependency(nodeId, useTemp, defTemp);
		dataDependency.add(dependencyTemp);
		
		use.put(nodeId, (HashSet<String>) useTemp.clone());
		useTemp.clear();
		def.put(nodeId, (HashSet<String>) defTemp.clone());
		defTemp.clear();
	}

}
