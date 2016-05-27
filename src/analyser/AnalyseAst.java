package analyser;

import java.io.IOException;

import org.json.simple.JSONObject;

import data.AST;
import output.Output;

public class AnalyseAst {
	private static AST ast;
	private static Visitor visitor;
	private static Output output;

	public static void main(String args[]){
		ast = new AST("json/ast2.json");
		System.out.println((JSONObject) ast.getTree());
		visitor = new Visitor(ast);
		System.out.println(visitor.getGraph());
		output = new Output(ast, visitor);
		try {
			output.printGraph("export/graph.dot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
