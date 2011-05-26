package RegAlloc;
import Graph.*;
public class InterferenceGraph extends Graph {
	Util.NodeTempMap nodeTemp = new Util.NodeTempMap();
	java.util.Set<Edge> adjSet = new java.util.HashSet();
	public InterferenceGraph(FlowGraph.FlowGraph flow){
		for(NodeList nodes = flow.nodes(); nodes != null; nodes = nodes.tail ){
			Temp.TempList tot = flow.tot(nodes.head);
			while(tot != null){
				if(tnode(tot.head) == null)
					addBind(newNode(), tot.head);
				tot= tot.tail;
			}
		}
	}
	public Node tnode(Temp.Temp temp){
		return nodeTemp.tnode(temp);
	}
	public Temp.Temp gtemp(Node node){
		return nodeTemp.gtemp(node);
	}
	public MoveList moves(){
		return null;
	}
	public void addBind(Node node, Temp.Temp temp){
		nodeTemp.put(node, temp);
	}
	public void addEdge(Node a, Node b){
		adjSet.add(new Edge(a,b));
		adjSet.add(new Edge(b,a));
		super.addEdge(a, b);
		super.addEdge(b, a);
	}
	public void addEdge(Temp.Temp a, Temp.Temp b){
		addEdge(tnode(a), tnode(b));
	}
	public boolean queryEdge(Node a, Node b){
		return adjSet.contains(new Edge(a,b));
	}
	public void newNode(Temp.Temp t){
		if(tnode(t) == null)
			addBind(newNode(), t);
	}
	public int spillCost(Node node) {return 1;}
}
