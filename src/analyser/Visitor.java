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
		
		exploreNode(mainBlock);
	}
	
	public void exploreNode(JSONArray currentNodeContent){
		
		for(int i = 0; i < currentNodeContent.size(); i++){
			JSONObject newNode = (JSONObject) currentNodeContent.get(i);
			switch((String) newNode.get("name")){
				case "If":
					// Do something for if node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "While":
					// Do something for while node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "For":
					// Do something for for node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "Switch":
					// Do something for switch node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				case "Return":
					// Do something for return node
					exploreNode((JSONArray) newNode.get("children"));
					break;
				default:
					//exploreNode((JSONArray) newNode.get("children"));
					break;
			}
		}
		
		return;
	}

	public JSONObject getMainFunction() {
		return mainFunction;
	}

	public void setMainFunction(JSONObject mainFunction) {
		this.mainFunction = mainFunction;
	}
}
