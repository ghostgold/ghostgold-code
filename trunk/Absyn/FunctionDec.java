package Absyn;
import Symbol.Symbol;
public class FunctionDec extends Dec {
	public Symbol name;
	public FieldList params;
	public NameTy result;  /* optional */
	public Exp body;
	public FunctionDec next;
	public FunctionDec(int p, Symbol n, FieldList a, NameTy r, Exp b, FunctionDec x)
	{pos=p; name=n; params=a; result=r; body=b; next=x;}
	public void append(FunctionDec t)
	{
		if(next == null)next = t;
		else next.append(t);
	}
}
