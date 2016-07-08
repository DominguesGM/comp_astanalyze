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
		case "TypeReferenceImpl":
			return content;
		case "ParameterReferenceImpl":
			return content;
		case "VariableReadImpl":
			output = processGeneric((JSONObject) children.get(1));
			
			// mark variable use
			useTemp.add(output);
			
			return output;
		case "LocalVariableImpl":
			output = processGeneric((JSONObject) children.get(0)) + " " + content;
			if(children.size() == 2){
				output += " = " + processGeneric((JSONObject) children.get(1));
				// mark variable definition
				defTemp.add(content);
			}

			return output;
		case "LiteralImpl":
			if(Tool.isNumeric(content))
				return content;
			else	return "\\\"" + content + "\\\""; // efectivelly having \" on the string is necessary for graphviz not to break reading the dot file
		case "LocalVariableReferenceImpl":
			return content;
		case "BinaryOperatorImpl":
			rightSide = processGeneric((JSONObject)children.get(2));
			leftSide = processGeneric((JSONObject)children.get(1));
			return leftSide + content + rightSide;
		case "AssignmentImpl":
			leftSide = processGeneric((JSONObject)children.get(1));			
			rightSide = processGeneric((JSONObject)children.get(2));
			return leftSide + " = " + rightSide;
		case "VariableWriteImpl":
			output = processGeneric((JSONObject)children.get(1));
			
			// mark variable definition
			defTemp.add(output);
			
			return output;
		case "OperatorAssignmentImpl":
			String rightHand = processGeneric((JSONObject)children.get(1));
			
			// mark variable definition
			useTemp.add(rightHand);
			
			output = rightHand +" "+ content +" "+ processGeneric((JSONObject)children.get(2));
			return processGeneric((JSONObject)children.get(1)) +" "+ content +" "+ processGeneric((JSONObject)children.get(2));
		case "UnaryOperatorImpl":
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
		case "ArrayTypeReferenceImpl":
			return processGeneric((JSONObject)children.get(0)) + "[]";
		case "NewArrayImpl":
			String array = "{";
			for(int i = 1; i < children.size(); i++){
				array += processGeneric((JSONObject)children.get(i));
				if(i + 1 < children.size())
					array += ", ";
			}
			return array + "}";
		case "ArrayWriteImpl":
			return processGeneric((JSONObject)children.get(1)) + "[" + processGeneric((JSONObject)children.get(2)) + "]";
		case "FieldReadImpl":
			if(children.size() == 3)
				return processGeneric((JSONObject)children.get(1)) + "." + processGeneric((JSONObject)children.get(2));
			else return processGeneric((JSONObject)children.get(1));
		case "FieldReferenceImpl":
			return content;
		case "InvocationImpl":
			String function = "";
			String thisElement = "";
			boolean foundExecutableReference = false;
			for(int i = 1; i < children.size(); i++){
				function += processGeneric((JSONObject)children.get(i));
				thisElement = (String) ((JSONObject)children.get(i)).get("name");
				if(foundExecutableReference){
					if(i + 1 < children.size())
						function += ", ";
				} else {
					if("ExecutableReferenceImpl".equals(thisElement)){
						foundExecutableReference = true;
						function += "(";
					} else {
						function += ".";
					}
				}
			}
			function += ")";
			return function;
		case "CatchVariableImpl":
			output = processGeneric((JSONObject) children.get(0)) + " " + content;
			if(children.size() == 2){
				output += " = " + processGeneric((JSONObject) children.get(1));
				// mark variable definition
				defTemp.add(content);
			}
			return output;
		case "ExecutableReferenceImpl":
			return content;
		case "BreakImpl":
			return type;
		case "ContinueImpl":
			return type;
		case "ReturnImpl":
			return type;
		default:
			return type;
		}
	}
}
