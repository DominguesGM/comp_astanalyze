package data;

import org.jgrapht.graph.DirectedPseudograph;

public class FlowGraph{
	private DirectedPseudograph<String, String> graph;
	
	public FlowGraph(){
		graph = new DirectedPseudograph<String, String>(String.class);
	}
	
	
}
