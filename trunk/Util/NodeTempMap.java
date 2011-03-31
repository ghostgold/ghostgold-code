package Util;
import Graph.Node;
import Temp.Temp;
public class NodeTempMap{
	java.util.Dictionary<Node, Temp> nodeToTemp = new java.util.Hashtable();
	java.util.Dictionary<Temp, Node> tempToNode = new java.util.Hashtable();
	public void put(Node node, Temp temp){
		nodeToTemp.put(node, temp);
		tempToNode.put(temp, node);
	}
	public Temp gtemp(Node node){
		return nodeToTemp.get(node);
	}
	public Node tnode(Temp temp){
		return tempToNode.get(temp);
	}
}