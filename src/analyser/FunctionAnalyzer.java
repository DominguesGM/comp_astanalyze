package analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONObject;

import javafx.util.Pair;

public class FunctionAnalyzer {

	
	private DirectedPseudograph<String, String> controlGraph;
	private DirectedPseudograph<String, String> dataGraph;
	
	private Analyzer parent;
	private CodeProcessor processor;
	
	private HashMap use;
	private HashMap def;
	private ArrayList<DataDependency> dataDependency;
	private HashSet<Pair<Pair<String, String>, String>> avoidEdgeDuplicates;
	private ArrayList<DataDependency> visitedPredecessors;

	public FunctionAnalyzer(Analyzer parent, String funcName, ArrayList<String> arguments, JSONObject funcCode, DirectedPseudograph<String, String> controlGraph, DirectedPseudograph<String, String> dataGraph){
		this.parent = parent;
		
		use = new HashMap<>();
		def = new HashMap<>();
		dataDependency = new ArrayList<DataDependency>();
		avoidEdgeDuplicates = new HashSet<Pair<Pair<String, String>, String> >();

		this.controlGraph = controlGraph;
		this.dataGraph = dataGraph;
		
		processor = new CodeProcessor(parent, funcName, arguments, funcCode, controlGraph, dataDependency, use, def);
				
		
		for(DataDependency node : dataDependency){
			ArrayList<DataDependency> tempPredecessors = new ArrayList<DataDependency>();
			for(String edgeName : controlGraph.incomingEdgesOf(node.getNode())){
				String nodeName = controlGraph.getEdgeSource(edgeName);
				tempPredecessors.add(getNodeDataDependency(nodeName));
			}
			node.setPredecessors(tempPredecessors);
		}
		
		processDependencies();
		
		generateDependencyGraph();
		
	}
	
	
	
	
	
	private void processDependencies() {
		for(DataDependency node : dataDependency){
			ArrayList<String> usages = new ArrayList(node.getUse());
			ArrayList<DataDependency> predecessorsToVisit = node.getPredecessors();
			Iterator<String> iVar= usages.iterator();
			
			//For each variable used in this node
			while(iVar.hasNext()){
				String tempVar = iVar.next();
				
				// If the var used in the node is also defined in the same node, add dependence and proceed to the preceding definitions
				if(node.defines(tempVar)){
					node.addDependency(node.getNode(), tempVar);
					if(!node.uses(tempVar))
						continue;
				}
				
				//check for a definition for each predecessor
				for(int i = 0; i < predecessorsToVisit.size(); i++){
					DataDependency tempPredecessor = predecessorsToVisit.get(i);
					visitedPredecessors = new ArrayList<DataDependency>();
					visitedPredecessors.add(tempPredecessor);
					if(tempPredecessor.defines(tempVar)){
						node.addDependency(tempPredecessor.getNode(), tempVar);
					} else {
						processDependencies(node, tempVar, tempPredecessor);
					}
					
				}
			}
		}
		
	}
	
	public void processDependencies(DataDependency node, String var, DataDependency currentNode){
		ArrayList<DataDependency> predecessorsToVisit = currentNode.getPredecessors();
		
		//check for a definition for each predecessor
		for(int i = 0; i < predecessorsToVisit.size(); i++){
			if(!visitedPredecessors.contains(predecessorsToVisit.get(i))){
				DataDependency tempPredecessor = predecessorsToVisit.get(i);
				visitedPredecessors.add(tempPredecessor);
				if(tempPredecessor.defines(var)){
					node.addDependency(tempPredecessor.getNode(), var);
				} else {
					processDependencies(node, var, tempPredecessor);
				}
			}
			
		}
	}
	
	public void generateDependencyGraph(){
		// Fill data dependency graph with all the nodes that affect dependencies
		for(DataDependency node : dataDependency){
			if(!node.getDef().isEmpty() || !node.getUse().isEmpty())
				dataGraph.addVertex(node.getNode());
		}
		// Link nodes that depend directly on eachother
		for(DataDependency node : dataDependency){
			avoidEdgeDuplicates.clear();
			ArrayList<String> dependencyNodes = node.getDependencyNodes();
			ArrayList<String> dependencyVars = node.getDependencyVars();
			if(dependencyNodes.size() != dependencyVars.size()){
				System.out.println("ERROR: dependencyNodes size is different from dependencyVars");
				return;
			}
			for(int i = 0; i < dependencyNodes.size(); i++){
				Pair<Pair<String, String>, String> nextEdge;
				nextEdge = new Pair<Pair<String, String>, String>(new Pair<String, String>(dependencyNodes.get(i), node.getNode()), dependencyVars.get(i));
				
				if(!avoidEdgeDuplicates.contains(nextEdge)){
					dataGraph.addEdge(dependencyNodes.get(i), node.getNode(), parent.newDataEdgeName() + ": " + dependencyVars.get(i));
					avoidEdgeDuplicates.add(nextEdge);
				}
				
			}
		}
	}
	
	public DataDependency getNodeDataDependency(String nodeName){
		for(DataDependency node : dataDependency){
			if(node.getNode().equals(nodeName)){
				return node;
			}
		}
		return null;
	}
}
