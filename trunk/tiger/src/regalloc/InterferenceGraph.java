package regalloc;
import graph.*;
public class InterferenceGraph extends Graph {
	util.NodeTempMap nodeTemp = new util.NodeTempMap();
	java.util.Set<Edge> adjSet = new java.util.HashSet();
	public InterferenceGraph(flowgraph.FlowGraph flow){
		for(NodeList nodes = flow.nodes(); nodes != null; nodes = nodes.tail ){
			temp.TempList tot = flow.tot(nodes.head);
			while(tot != null){
				if(tnode(tot.head) == null)
					addBind(newNode(), tot.head);
				tot= tot.tail;
			}
		}
	}
	public Node tnode(temp.AtomicTemp temp){
		return nodeTemp.tnode(temp);
	}
	public temp.AtomicTemp gtemp(Node node){
		return nodeTemp.gtemp(node);
	}
	public MoveList moves(){
		return null;
	}
	public void addBind(Node node, temp.AtomicTemp temp){
		nodeTemp.put(node, temp);
	}
	public void addEdge(Node a, Node b){
		adjSet.add(new Edge(a,b));
		adjSet.add(new Edge(b,a));
		super.addEdge(a, b);
		super.addEdge(b, a);
	}
	public void addEdge(temp.AtomicTemp a, temp.AtomicTemp b){
		addEdge(tnode(a), tnode(b));
	}
	public boolean queryEdge(Node a, Node b){
		return adjSet.contains(new Edge(a,b));
	}
	public void newNode(temp.AtomicTemp t){
		if(tnode(t) == null)
			addBind(newNode(), t);
	}
	public int spillCost(Node node) {return 1;}
}
