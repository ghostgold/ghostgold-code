package absyn;
import symbol.Symbol;
public class TypeDec extends Dec {
	public Symbol name;
	public Ty ty;
	public TypeDec next;
	public TypeDec(int p, Symbol n, Ty t, TypeDec x) {pos=p; name=n; ty=t; next=x;}
	public void append(TypeDec t)
	{
		if(next == null)next = t;
		else next.append(t);
	}
	public TypeDec clone(){
		if(next != null)return new  TypeDec(pos, name, ty.clone(), next.clone());
		return new  TypeDec(pos, name, ty.clone(), null);
	}
}
