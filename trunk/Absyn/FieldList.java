package Absyn;
import Symbol.Symbol;
import Util.BoolList;
public class FieldList extends Absyn {
	public Symbol name;
	public Symbol typ;
	public FieldList tail;
	public boolean escape = true;
	public FieldList(int p, Symbol n, Symbol t, FieldList x) {pos=p; name=n; typ=t; tail=x;}
	public void append(FieldList t)
	{
		if(tail == null)tail = t;
		else tail.append(t);
	}
	public BoolList getEscape(){
		if(tail == null)return new BoolList(escape, null);
		else return new BoolList(escape, tail.getEscape());
	}
}
