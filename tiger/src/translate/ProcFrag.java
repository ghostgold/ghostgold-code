package translate;
public class ProcFrag extends Frag{
	public ProcFrag(tree.Stm b, frame.Frame f, Frag next){
		super(next);
		body = b;
		frame = f;
	}
	public tree.Stm body;
	public frame.Frame frame;
}