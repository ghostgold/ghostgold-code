package absyn;
import symbol.Symbol;
public class ExpList {
	public Exp head;
	public ExpList tail;
	public ExpList(Exp h, ExpList t) {head=h; tail=t;}
	public void append(Exp h)
	{
		if(tail==null)tail = new ExpList(h, null);
		else tail.append(h);
	}
	public ExpList clone(){
		if(tail == null)return new ExpList(head.clone(), null);
		else return new ExpList(head.clone(), tail.clone());
	}
}
