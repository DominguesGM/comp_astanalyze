package test;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import analyser.AnalyseAst;
import analyser.Visitor;
import data.AST;

public class JsonInput {
	
	public static void main(String args[]){
		AST newAST = new AST("json\\ast.json");
		System.out.println((JSONObject) newAST.getTree());
		System.out.println(((JSONArray) AnalyseAst.getAST().getTree().get("children")));
		Visitor controlFlow = new Visitor(newAST);
	}
	
}
