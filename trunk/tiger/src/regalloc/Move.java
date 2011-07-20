package regalloc;
public class Move{
	graph.Node dst;
	graph.Node src;
	public Move(graph.Node d, graph.Node s){
		dst = d;
		src = s;
	}
}