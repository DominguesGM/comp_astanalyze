package analyser;

import data.AST;
import other.Log;

public class AstAnalyze {
	private static AST ast;
	private static Analyzer analyzer;

	public static void main(String args[]){
		if(args.length != 1){
			Log.error("AstAnalyze usage: AstAnalyze <ast_file.json>");
			return;
		}
		
		try{
			ast = new AST(args[0]);
		} catch(Exception e){
			Log.error("Error occurred while parsing json file");
			return;
		}
		
		try{
			analyzer = new Analyzer(ast);
			analyzer.analyze();
		} catch(Exception e){
			Log.error("Error while analyzing the abstract syntax tree. Check json format.");
			e.printStackTrace();
			return;
		}
	}
	
	public static AST getAST(){
		return ast;
	}

	public static Analyzer getAnalyzer() {
		return analyzer;
	}

	public static void setAnalyzer(Analyzer analyzer) {
		AstAnalyze.analyzer = analyzer;
	}	
}
