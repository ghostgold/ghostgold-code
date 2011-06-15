package Absyn;
import Symbol.Symbol;
public class DecList {
	public Dec head;
	public DecList tail;
	public DecList(Dec h, DecList t) {head=h; tail=t;}
	public void append(Dec t)
	{
		if(tail == null){
			if((head instanceof FunctionDec) && (t instanceof FunctionDec))((FunctionDec)head).append((FunctionDec)t);
			else if ((head instanceof TypeDec) && (t instanceof TypeDec))((TypeDec)head).append((TypeDec)t);
			else tail = new DecList(t,null);
		}
		else tail.append(t);
	}
	public DecList clone(){
		if(tail == null)return new DecList(head.clone(), null);
		else return new DecList(head.clone(), tail.clone());
	}
}
