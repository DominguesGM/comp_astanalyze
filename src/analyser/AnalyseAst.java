package analyser;

import org.json.simple.JSONObject;

import data.AST;

public class AnalyseAst {
	private static AST ast;
	private static Visitor visitor;

	public static void main(String args[]){
		ast = new AST("json/ast2.json");
		System.out.println((JSONObject) ast.getTree());
		visitor = new Visitor(ast);
		System.out.println(visitor.getGraph());
	}
	
	public static AST getAST(){
		return ast;
	}

	public static Visitor getVisitor() {
		return visitor;
	}

	public static void setVisitor(Visitor visitor) {
		AnalyseAst.visitor = visitor;
	}
	
	
}
