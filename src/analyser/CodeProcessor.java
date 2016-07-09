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
	private boolean setExitLoopControl = false;
	
	private CodeGenerator generator;
	private Analyzer parent;
	
	
	public CodeProcessor(Analyzer parent, String funcName, ArrayList<String> arguments, JSONObject funcCode, DirectedPseudograph<String, String> graph, 
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
		ArrayList<String> finalNodes = exploreNode(funcCode, functionName, true);
		finalNodes.addAll(returnNodes);
		String exitNodeName = parent.newNodeName() + ": exit";
		graph.addVertex(exitNodeName);
		
		for(String node : finalNodes){
			graph.addEdge(node, exitNodeName, parent.newEdgeName());
		}
	}
	
	
	public ArrayList<String> exploreNode(JSONObject currentNode, ArrayList<String> prevStartNodes, boolean forTerminator){
			ArrayList<String> startNodeList = prevStartNodes;
			ArrayList<String> exitNodesList = new ArrayList<String>();
			ArrayList<String> argumentList = new ArrayList<String>();
			String firstNode=null;
			
			JSONArray currentNodeContent = (JSONArray) currentNode.get("children");
			String childStartingNode;
			String condition;
			int i = 0;
			if(currentNode.get("name").equals("CaseImpl")){
				JSONObject caseNode = (JSONObject) currentNodeContent.get(0);
				if(((String) ((JSONObject) currentNodeContent.get(0)).get("name")).equals("LiteralImpl")){
					childStartingNode = this.parent.newNodeName() + ": Case " + generator.processGeneric((JSONObject) currentNodeContent.get(0));
					i = 1;
				}else{
					childStartingNode = this.parent.newNodeName() + ": Default";
				}
				graph.addVertex(childStartingNode);
				if(!forTerminator)
					firstNode = childStartingNode;
				for(String node : startNodeList){
					graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
				}
				exitNodesList.add(childStartingNode);
				
				saveDataFlow(childStartingNode);
			} else{
				if(startNodeList != null)
					exitNodesList.addAll(startNodeList);
			}
			if(currentNode.get("name").equals("CaseImpl") || currentNode.get("name").equals("BlockImpl")){
				for(; i < currentNodeContent.size(); i++){
					condition = null;
					JSONObject newNode = (JSONObject) currentNodeContent.get(i);
					if(i == 0 && !forTerminator){
						exitNodesList = processNode(newNode, exitNodesList, argumentList, forTerminator);
						firstNode = exitNodesList.get(0);
					} else {
						exitNodesList = processNode(newNode, exitNodesList, argumentList, true);
					}
					if(getExitLoopControl() && !currentNode.get("name").equals("CaseImpl")){
						break;
					}
					if(i == 0 && !forTerminator){
						firstNode = exitNodesList.get(0);
					}
				}
			} else {
				exitNodesList = processNode(currentNode, exitNodesList, argumentList, forTerminator);
			}
			if(!forTerminator)
				exitNodesList.add(0, firstNode);
			return exitNodesList;
	}
	
	
	public ArrayList<String> processNode(JSONObject newNode, ArrayList<String> exitNodesListParam, ArrayList<String> argumentList, boolean prevForTerminator){
		ArrayList<String> exitNodesList = new ArrayList<String>(exitNodesListParam);
		String childStartingNode;
		String condition = "";
		String firstNode= null;
		switch((String) newNode.get("name")){
		case "IfImpl":
			// process condition and create condition node
			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			childStartingNode = this.parent.newNodeName() + ": if(" + condition + ")";
			graph.addVertex(childStartingNode);
			firstNode = childStartingNode;
			
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
			exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), argumentList, true));
			// if second block exists, process
			if(((JSONArray) newNode.get("children")).size() > 2){
				// Process Else (second block)
				argumentList.clear();
				argumentList.add(childStartingNode);
				exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(2), argumentList, true));
			} else {
				exitNodesList.add(childStartingNode);
			}
			break;
		case "WhileImpl":
			// process condition and create condition node
			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			childStartingNode = this.parent.newNodeName() + ": while(" + condition + ")";
			graph.addVertex(childStartingNode);
			firstNode = childStartingNode;
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}

			// reset exitNodesList array and process code block
			argumentList.clear();
			argumentList.add(childStartingNode);
			exitNodesList = exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), argumentList, true);

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
		case "ForImpl":
			String assignment = null;
			condition = null;
			String statement = null;
			String assignmentNode = null;
			String conditionNode = null;
			String statementNode = null;
			boolean forTerminate = false;
			
			
			for(int i = 0; i+1 < ((JSONArray) newNode.get("children")).size(); i++){
				String statementType = (String) ((JSONObject) ((JSONArray) newNode.get("children")).get(i)).get("name");
				if(statementType.equals("AssignmentImpl") || statementType.equals("UnaryOperatorImpl") || statementType.equals("LocalVariableImpl")){
					if(i == 0){
						assignment = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(i));
					} else {
						statement = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(i));
					}
				}
				if(((JSONObject) ((JSONArray) ((JSONObject) ((JSONArray) newNode.get("children")).get(i)).get("children")).get(0)).get("content").equals("boolean")){
					forTerminate = true;
					condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(i));
				}
			}
			
			// process condition and create condition node
//			assignment = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			if(assignment != null){
				assignmentNode = this.parent.newNodeName() + ": " + assignment;
				graph.addVertex(assignmentNode);
				
				//Dataflow related
				saveDataFlow(assignmentNode);
				
				// connect condition node to previous child end nodes
				for(String node : exitNodesList){
					graph.addEdge(node, assignmentNode, this.parent.newEdgeName());
				}

				// reset exitNodesList array
				exitNodesList.clear();
				exitNodesList.add(assignmentNode);
			}
			
			// process condition and create condition node
//			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(1));
			if(condition != null){
				conditionNode = this.parent.newNodeName() + ": " + condition;
				graph.addVertex(conditionNode);
				
				//Dataflow related
				saveDataFlow(conditionNode);
				
				// connect condition node to previous child end nodes
				for(String node : exitNodesList){
					graph.addEdge(node, conditionNode, this.parent.newEdgeName());
				}

				// reset exitNodesList array
				exitNodesList.clear();
				exitNodesList.add(conditionNode);
			
			}
			
			// Explore For code block
			argumentList.clear();
			argumentList.addAll(exitNodesList);
			exitNodesList.clear();
			exitNodesList.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(3), argumentList, forTerminate));
			
			if(assignment!= null)
				firstNode = assignmentNode;
			else{
				if(forTerminate)
					firstNode=conditionNode;
				else
					firstNode=exitNodesList.get(0);
			}

			
			String continueDestination =null;
			// process statement and create statement node
//			statement = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(2));
			if(statement != null){
				statementNode = this.parent.newNodeName() + ": " + statement;
				graph.addVertex(statementNode);
				
				continueDestination = statementNode;

				// connect condition node (if exists) to previous child end nodes with statement
				int j;

				if(forTerminate){
					graph.addEdge(statementNode, conditionNode, this.parent.newEdgeName());
					j=0;
				}else{
					graph.addEdge(statementNode, exitNodesList.get(0), this.parent.newEdgeName());
					j=1;
				}
				for(; j < exitNodesList.size(); j++){
					graph.addEdge(exitNodesList.get(j), statementNode, this.parent.newEdgeName());
				}
			} else{
				// connect condition node (if exists) to previous child end nodes without statement
				if(forTerminate){
					continueDestination = conditionNode;
					for(int j = 0; j < exitNodesList.size(); j++){
						graph.addEdge(exitNodesList.get(j), conditionNode, this.parent.newEdgeName());
					}
				}else{
					continueDestination = exitNodesList.get(0);
					for(int j = 1; j < exitNodesList.size(); j++){
						graph.addEdge(exitNodesList.get(j), exitNodesList.get(0), this.parent.newEdgeName());
					}
				}
			}
			
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
		case "DoImpl":
			String doStartNode = this.parent.newNodeName() + ": do";
			// process condition and create condition node
			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			conditionNode = this.parent.newNodeName() + ": while(" + condition+");";
			firstNode = doStartNode;
			
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
			exitNodesList = exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1), argumentList, true);

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
		case "SwitchImpl":
			// process condition and create condition node
			condition = generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			childStartingNode = this.parent.newNodeName() + ": switch(" + condition + ")";
			graph.addVertex(childStartingNode);
			firstNode = childStartingNode;
			
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
				lastCaseElement.addAll(exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(j), argumentList, true));
			}
			
			exitNodesList.addAll(lastCaseElement);
			exitNodesList.addAll(breakNodes);
			breakNodes.clear();
			setExitLoopControl(false);
			break;
		case "ReturnImpl":
			String returnContent = "";
			if(((JSONArray) newNode.get("children")).size() != 0)
				returnContent = " " + generator.processGeneric((JSONObject) ((JSONArray) newNode.get("children")).get(0));
			childStartingNode = this.parent.newNodeName()+": return" + returnContent;
			graph.addVertex(childStartingNode);
			returnNodes.add(childStartingNode);
			firstNode = childStartingNode;
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}
			
			// reset exitNodesList array
			exitNodesList.clear();
			setExitLoopControl(true);
			break;
		case "ContinueImpl":
			childStartingNode = this.parent.newNodeName()+": continue";
			graph.addVertex(childStartingNode);
			continueNodes.add(childStartingNode);
			
			firstNode = childStartingNode;
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}
			
			// reset exitNodesList array
			exitNodesList.clear();
			setExitLoopControl(true);
			break;
		case "BreakImpl":
			childStartingNode = this.parent.newNodeName()+": break";
			graph.addVertex(childStartingNode);
			breakNodes.add(childStartingNode);
			
			firstNode = childStartingNode;
			
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
			
			firstNode = childStartingNode;
			
			saveDataFlow(childStartingNode);
			
			// connect condition node to previous child end nodes
			for(String node : exitNodesList){
				graph.addEdge(node, childStartingNode, this.parent.newEdgeName());
			}
			
			exitNodesList.clear();
			exitNodesList.add(childStartingNode);
			break;
		}
		if(!prevForTerminator)
			exitNodesList.add(0, firstNode);
		
		
		return exitNodesList;
	}
	
	private void setExitLoopControl(boolean var){
		this.setExitLoopControl = var;
	}
	
	private boolean getExitLoopControl(){
		return this.setExitLoopControl ;
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
