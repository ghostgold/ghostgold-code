package Absyn;
import Symbol.Symbol;
public class FieldExpList extends Absyn {
	public Symbol name;
	public Exp init;
	public FieldExpList tail;
	public FieldExpList(int p, Symbol n, Exp i, FieldExpList t) {pos=p; 
		name=n; init=i; tail=t;
	}
	public void append(FieldExpList t)
	{
		if(tail == null)tail = t;
		else tail.append(t);
	}
}
