package util;
import temp.AtomicTemp;
import graph.Node;
public class NodeTempMap{
	java.util.Dictionary<Node, AtomicTemp> nodeToTemp = new java.util.Hashtable();
	java.util.Dictionary<AtomicTemp, Node> tempToNode = new java.util.Hashtable();
	public void put(Node node, AtomicTemp temp){
		nodeToTemp.put(node, temp);
		tempToNode.put(temp, node);
	}
	public AtomicTemp gtemp(Node node){
		return nodeToTemp.get(node);
	}
	public Node tnode(AtomicTemp temp){
		return tempToNode.get(temp);
	}
}