package tree;
import temp.AtomicLabel;
import temp.AtomicTemp;
public class Mem extends Exp {
	public Exp exp;
	public Mem(Exp e) {exp=e;}
	public ExpList kids() {return new ExpList(exp,null);}
	public Exp build(ExpList kids) {
		return new Mem(kids.head);
	}
}

