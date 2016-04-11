package analyser;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import data.AST;

public class Visitor {
	private JSONObject mainFunction;
	
	public Visitor(AST ast){
		JSONObject rootPackage = (JSONObject) ((JSONArray) AnalyseAst.getAST().getTree().get("children")).get(0);
		JSONObject packageMain = (JSONObject) ((JSONArray) rootPackage.get("children")).get(0);
		JSONObject mainClass = (JSONObject) ((JSONArray) packageMain.get("children")).get(0);
		JSONObject mainFunc = (JSONObject) ((JSONArray) mainClass.get("children")).get(1);
		JSONObject mainFuncCode = (JSONObject) ((JSONArray) mainFunc.get("children")).get(2);
		JSONArray mainBlock = (JSONArray) mainFuncCode.get("children");
	
		
		for(int i = 0; i < mainBlock.size(); i++){
			JSONObject newNode = (JSONObject) mainBlock.get(i);
			switch((String) newNode.get("content")){
				case "if":
					// Do something for if node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "while":
					// Do something for while node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "for":
					// Do something for for node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "switch":
					// Do something for switch node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "return":
					// Do something for return node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				default:
					break;
			}
		}
	}
	
	public void exploreNode(JSONArray currentNodeContent){
		
		for(int i = 0; i < currentNodeContent.size(); i++){
			JSONObject newNode = (JSONObject) currentNodeContent.get(i);
			switch((String) newNode.get("content")){
				case "if":
					// Do something for if node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "while":
					// Do something for while node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "for":
					// Do something for for node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "switch":
					// Do something for switch node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "return":
					// Do something for return node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				default:
					break;
			}
		}
		
		return;
	}
}
