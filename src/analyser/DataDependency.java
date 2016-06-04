package analyser;

import java.util.ArrayList;
import java.util.HashSet;

public class DataDependency implements Comparable<DataDependency>{
	private String node;
	private HashSet<String> use;
	private HashSet<String> def;
	private ArrayList<DataDependency> predecessors;
	private ArrayList<String> dependencyNodes;
	private ArrayList<String> dependencyVars;
	
	
	public DataDependency(String node){
		this.node = node;
		use = new HashSet<String>();
		def = new HashSet<String>();
		dependencyNodes = new ArrayList<String>();
		dependencyVars = new ArrayList<String>();
		predecessors = new ArrayList<DataDependency>();
	}
	
	public DataDependency(String node, HashSet<String> use, HashSet<String> def){
		this.node = node;
		this.use = (HashSet<String>) use.clone();
		this.def = (HashSet<String>) def.clone();
		dependencyNodes = new ArrayList<String>();
		dependencyVars = new ArrayList<String>();
		predecessors = new ArrayList<DataDependency>();
	}
	
	@Override
	public int compareTo(DataDependency o) {
		return this.node.compareTo(o.getNode());
	}
	
	public boolean uses(String var){
		for(String uses : use){
			if(uses.equals(var))
				return true;
		}
		return false;
	}
	
	public boolean defines(String var){
		for(String defines : this.def){
			if(defines.equals(var))
				return true;
		}
		return false;
	}




	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public HashSet<String> getUse() {
		return use;
	}
	public void setUse(HashSet<String> use) {
		this.use = use;
	}
	public HashSet<String> getDef() {
		return def;
	}
	public void setDef(HashSet<String> def) {
		this.def = def;
	}
	public void addUse(String var) {
		this.use.add(var);
	}
	public void addDef(String var) {
		this.def.add(var);
	}
	public void setPredecessors(ArrayList<DataDependency> array){
		this.predecessors = array;
	}
	public ArrayList<DataDependency> getPredecessors(){
		return this.predecessors;
	}
	public void addDependency(String node, String var){
		this.dependencyNodes.add(node);
		this.dependencyVars.add(var);
	}
	public ArrayList<String> getDependencyNodes(){
		return this.dependencyNodes;
	}
	public ArrayList<String> getDependencyVars(){
		return this.dependencyVars;
	}
	
}
