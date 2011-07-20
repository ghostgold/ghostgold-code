package regalloc;

public class MoveList {
	//   public Graph.Node src, dst;
	public Move head;
   public MoveList tail;
   public MoveList(graph.Node s, graph.Node d, MoveList t) {
	   this(new Move(s, d), t);
   }
	public MoveList(Move h, MoveList t){
		head = h;
		tail = t;
	}
}

