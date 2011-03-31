package RegAlloc;
public class Move{
	Graph.Node dst;
	Graph.Node src;
	public Move(Graph.Node d, Graph.Node s){
		dst = d;
		src = s;
	}
}