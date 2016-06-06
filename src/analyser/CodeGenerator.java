package analyser;

import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import other.Tool;

public class CodeGenerator {
	
	private HashSet<String> useTemp;
	private HashSet<String> defTemp;
	
	public CodeGenerator(HashSet<String> useTemp, HashSet<String> defTemp){
		this.useTemp = useTemp;
		this.defTemp = defTemp;
	}
	
	
	public String processGeneric(JSONObject node) {
		String type = (String) node.get("name");
		String content = (String) node.get("content");
		JSONArray children = (JSONArray) node.get("children");
		String leftSide = null;
		String rightSide = null;
		String output = null;
		
		switch(type){
		case "TypeReference":
			return content;
		case "ParameterReference":
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
				// mark variable definition
				defTemp.add(content);
			}

			return output;
		case "Literal":
			if(Tool.isNumeric(content))
				return content;
			else	return "\\\"" + content + "\\\""; // efectivelly having \" on the string is necessary for graphviz not to break reading the dot file
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
			String rightHand = processGeneric((JSONObject)children.get(1));
			
			// mark variable definition
			useTemp.add(rightHand);
			
			output = rightHand +" "+ content +" "+ processGeneric((JSONObject)children.get(2));
			return processGeneric((JSONObject)children.get(1)) +" "+ content +" "+ processGeneric((JSONObject)children.get(2));
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
		case "ArrayTypeReference":
			return processGeneric((JSONObject)children.get(0)) + "[]";
		case "NewArray":
			String array = "{";
			for(int i = 1; i < children.size(); i++){
				array += processGeneric((JSONObject)children.get(i));
				if(i + 1 < children.size())
					array += ", ";
			}
			return array + "}";
		case "ArrayWrite":
			return processGeneric((JSONObject)children.get(1)) + "[" + processGeneric((JSONObject)children.get(2)) + "]";
		case "FieldRead":
			if(children.size() == 3)
				return processGeneric((JSONObject)children.get(1)) + "." + processGeneric((JSONObject)children.get(2));
			else return processGeneric((JSONObject)children.get(1));
		case "FieldReference":
			return content;
		case "Invocation":
			String function = "";
			String thisElement = "";
			boolean foundExecutableReference = false;
			for(int i = 1; i < children.size(); i++){
				function += processGeneric((JSONObject)children.get(i));
				thisElement = (String) ((JSONObject)children.get(i)).get("name");
				if(foundExecutableReference){
					if(i + 1 < children.size())
						function += ", ";
					else
						function += ")";
				} else {
					if("ExecutableReference".equals(thisElement)){
						foundExecutableReference = true;
						function += "(";
					} else {
						function += ".";
					}
				}
			}
			return function;
		case "ExecutableReference":
			return content;
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
}
