package output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringEdgeNameProvider;
import org.jgrapht.ext.StringNameProvider;
import analyser.Visitor;
import data.AST; 

public class Output {
	public static AST ast;
	public static Visitor visitor;
	
	public Output(AST a, Visitor v){
		ast = a;
		visitor = v;
	}
	
	public void printGraph(String filename) throws IOException {
		FileWriter fstream = new FileWriter(filename);
		BufferedWriter out = new BufferedWriter(fstream);
		DOTExporter<String, String> exporter = new DOTExporter<String, String>(
				new IntegerNameProvider<String>(),
				new StringNameProvider<String>(),
				new StringEdgeNameProvider<String>());
		exporter.export(out, visitor.getGraph());
	}
}
