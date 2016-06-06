package analyser;

import java.io.IOException;

import org.json.simple.JSONObject;

import data.AST;
import other.Log;
import output.Output;

public class AstAnalyze {
	private static AST ast;
	private static Visitor visitor;
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
		Log.info("json file read");
		String delims = "[.]";
		String[] temptokens = args[0].split("[.]");
		String[] tokens = temptokens[0].split("[/]");
		if(tokens.length == 1)
			tokens = temptokens[0].split("[\\\\]");
		visitor = new Visitor(ast);
		output = new Output(visitor);
		System.out.println(tokens[1]);
		try {
			output.printControlGraph("export/"+tokens[1]+"_control.dot");
			output.printDataGraph("export/"+tokens[1]+"_data.dot");
		} catch (IOException e) {
			Log.error("Error outputing dot files");
			return;
		}
	}
	
	public static AST getAST(){
		return ast;
	}

	public static Visitor getVisitor() {
		return visitor;
	}

	public static void setVisitor(Visitor visitor) {
		AstAnalyze.visitor = visitor;
	}
	
	
}
