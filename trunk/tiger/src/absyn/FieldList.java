package absyn;
import symbol.Symbol;
import util.BoolList;
public class FieldList extends Absyn {
	public Symbol name;
	public Symbol typ;
	public FieldList tail;
	public boolean escape = true;
	public boolean isConst = true;
	public FieldList(int p, Symbol n, Symbol t, FieldList x) {pos=p; name=n; typ=t; tail=x;}
	public FieldList(int p, Symbol n, Symbol t, FieldList x, boolean esc, boolean isCon) {pos=p; name=n; typ=t; tail=x;escape = esc; isConst = isCon;}
	public void append(FieldList t)
	{
		if(tail == null)tail = t;
		else tail.append(t);
	}
	public BoolList getEscape(){
		if(tail == null)return new BoolList(escape, null);
		else return new BoolList(escape, tail.getEscape());
	}
	public FieldList clone(){
		if(tail == null)return new FieldList(pos, name, typ, null, escape, isConst);
		else return new FieldList(pos, name, typ, tail.clone(), escape, isConst);
	}
}
