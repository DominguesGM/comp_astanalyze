package analyser;
import org.jgrapht.graph.DirectedMultigraph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import data.AST;

public class Visitor {
	private JSONObject mainFunction;
	private DirectedMultigraph<String, String> graph;
	
	public Visitor(AST ast){
		JSONObject rootPackage = (JSONObject) ((JSONArray) AnalyseAst.getAST().getTree().get("children")).get(0);
		JSONObject packageMain = (JSONObject) ((JSONArray) rootPackage.get("children")).get(0);
		JSONObject mainClass = (JSONObject) ((JSONArray) packageMain.get("children")).get(0);
		JSONObject mainFunc = (JSONObject) ((JSONArray) mainClass.get("children")).get(1);
		JSONObject mainFuncCode = (JSONObject) ((JSONArray) mainFunc.get("children")).get(2);
		JSONArray mainBlock = (JSONArray) mainFuncCode.get("children");
		
		int[] level_position = {1};
		int level = 0;
		
		graph = new DirectedMultigraph<String,String>(String.class);
		addNode(level_position);
	
		
		for(int i = 0; i < mainBlock.size(); i++){
			JSONObject newNode = (JSONObject) mainBlock.get(i);
			switch((String) newNode.get("content")){
				case "if":
					// process condition
					String ifString = processContition((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					// process first block
					
					exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1));
					// if second block exists, process
					if(((JSONArray) newNode.get("children")).size() > 2){
						// Process Else (second block)
						exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(2));
					}
					
					break;
				case "while":
					// Do something for while node
					exploreNode(newNode);
					break;
				case "for":
					// Do something for for node
					exploreNode(newNode);
					break;
				case "switch":
					// Do something for switch node
					exploreNode(newNode);
					break;
				case "return":
					// Do something for return node
					exploreNode(newNode);
					break;
				default:
					break;
			}
		}
	}
	
	public void exploreNode(JSONObject currentNode){
		JSONArray currentNodeContent = (JSONArray) currentNode.get("children");
		for(int i = 0; i < currentNodeContent.size(); i++){
			JSONObject newNode = (JSONObject) currentNodeContent.get(i);
			switch((String) newNode.get("content")){
				case "if":
					// process condition
					processContition((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					// process first block
					exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1));
					// if second block exists, process
					if(((JSONArray) newNode.get("children")).size() > 2){
						// Process Else (second block)
						exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(2));
					}
					break;
				case "while":
					// process condition
					processContition((JSONObject) ((JSONArray) newNode.get("children")).get(0));
					// process code block
					exploreNode((JSONObject) ((JSONArray) newNode.get("children")).get(1));
					break;
				case "for":
					// Do something for for node
					exploreNode(newNode);
					break;
				case "switch":
					// Do something for switch node
					exploreNode(newNode);
					break;
				case "return":
					// Do something for return node
					exploreNode(newNode);
					break;
				default:
					break;
			}
		}
		
		return;
	}

	private String processContition(JSONObject conditionalNode) {
		String node = null;
		switch((String) conditionalNode.get("name")){
		case "BinaryOperator":
			if(((JSONObject)((JSONArray) conditionalNode.get("children")).get(0)).get("content").equals("boolean")){
				String rightSide = processGeneric((JSONObject)((JSONArray) conditionalNode.get("children")).get(2));
				String leftSide = processGeneric((JSONObject)((JSONArray) conditionalNode.get("children")).get(1));
				node = leftSide + conditionalNode.get("content") + rightSide;
			}
		}
		return node;
	}

	private String processGeneric(JSONObject node) {
		String type = (String) node.get("name");
		String content = (String) node.get("content");
		JSONArray children = (JSONArray) node.get("children");
		
		switch(type){
		case "TypeReference":
			return content;
		case "VariableRead":
			return processGeneric((JSONObject) children.get(1));
		case "Literal":
			return content;
		case "LocalVariableReference":
			return content;
		case "BinaryOperator":
			String rightSide = processGeneric((JSONObject)node.get(2));
			String leftSide = processGeneric((JSONObject)node.get(1));
			return leftSide + content + rightSide;
		}
		
		return null;
	}
	
	private String addNode(int[] level_position){
		String nodeContent = "Block_";
		for(int i = 0; i < level_position.length - 1; i++){
			nodeContent += level_position[i] + ".";
		}
		nodeContent += level_position[level_position.length - 1];
		graph.addVertex(nodeContent);
		return nodeContent;
		
	}
}
