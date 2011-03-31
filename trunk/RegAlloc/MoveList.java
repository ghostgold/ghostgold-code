package RegAlloc;

public class MoveList {
	//   public Graph.Node src, dst;
	public Move head;
   public MoveList tail;
   public MoveList(Graph.Node s, Graph.Node d, MoveList t) {
	   this(new Move(s, d), t);
   }
	public MoveList(Move h, MoveList t){
		head = h;
		tail = t;
	}
}

