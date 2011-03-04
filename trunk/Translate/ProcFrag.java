package Translate;
public class ProcFrag extends Frag{
	public ProcFrag(Tree.Stm b, Frame.Frame f, Frag next){
		super(next);
		body = b;
		frame = f;
	}
	Tree.Stm body;
	Frame.Frame frame;
}