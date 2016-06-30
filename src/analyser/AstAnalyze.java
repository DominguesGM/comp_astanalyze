package analyser;

import java.io.File;
import java.io.IOException;

import data.AST;
import other.Log;
import output.Output;

public class AstAnalyze {
	private static AST ast;
	private static Analyzer analyzer;
	private static Output output;

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
		File theFile = new File(args[0]);
		
		try{
			analyzer = new Analyzer(ast);
			analyzer.analyze();
		} catch(Exception e){
			Log.error("Error while analyzing the abstract syntax tree. Check json format.");
			return;
		}
		output = new Output(analyzer);
		System.out.println(theFile.getName());
		try {
			output.printControlGraph("export/"+theFile.getName()+"_control.dot");
			output.printDataGraph("export/"+theFile.getName()+"_data.dot");
		} catch (IOException e) {
			Log.error("Error outputing dot files");
			return;
		}
		Log.info("Both dot files generated: ");
		Log.info("export/"+theFile.getName()+"_control.dot");
		Log.info("export/"+theFile.getName()+"_data.dot");
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
