package graph;
public class Edge{
	Node from;
	Node to;
	public Edge(Node f, Node t){
		from = f; 
		to = t;
	}
	@Override
	public boolean equals(Object e){
		if(!(e instanceof Edge))return false;

		if(this.from.equals(((Edge)e).from) && this.to.equals(((Edge)e).to))return true;
		return false;
	}
	@Override
	public int hashCode(){
		return from.hashCode()*2333 + to.hashCode();
	}
}
