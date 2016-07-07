package output;

import analyser.ClassAnalyzer;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringEdgeNameProvider;
import org.jgrapht.ext.StringNameProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Output {
	public static ClassAnalyzer visitor;
	
	public Output(ClassAnalyzer v, String path){
		visitor = v;

		File exportDir = new File(path);

		// if the directory does not exist, create it
		if (!exportDir.exists()) {
			try{
				exportDir.mkdir();
			}
			catch(SecurityException se){
				//handle it
			}
		}
	}
	
	public void printControlGraph(String filename) throws IOException {
		FileWriter fstream = new FileWriter(filename);
		BufferedWriter out = new BufferedWriter(fstream);
		DOTExporter<String, String> exporter = new DOTExporter<String, String>(
				new IntegerNameProvider<String>(),
				new StringNameProvider<String>(),
				new StringEdgeNameProvider<String>());
		exporter.export(out, visitor.getControlGraph());
	}
	
	public void printDataGraph(String filename) throws IOException {
		FileWriter fstream = new FileWriter(filename);
		BufferedWriter out = new BufferedWriter(fstream);
		DOTExporter<String, String> exporter = new DOTExporter<String, String>(
				new IntegerNameProvider<String>(),
				new StringNameProvider<String>(),
				new StringEdgeNameProvider<String>());
		exporter.export(out, visitor.getDataGraph());
	}
}
