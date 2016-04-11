package analyser;

import data.AST;

public class AnalyseAst {
	private static AST ast;

	public static void main(String args[]){
		ast = new AST("json/ast.json");
	}
	
	public static AST getAST(){
		return ast;
	}
	
	
}
