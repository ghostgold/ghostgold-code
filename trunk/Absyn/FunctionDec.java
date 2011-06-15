package Absyn;
import Symbol.Symbol;
public class FunctionDec extends Dec {
	public Symbol name;
	public FieldList params;
	public NameTy result;  /* optional */
	public Exp body;
	public FunctionDec next;
	public boolean call = false;
	public int outGoing;
	public FunctionDec(int p, Symbol n, FieldList a, NameTy r, Exp b, FunctionDec x)
	{pos=p; name=n; params=a; result=r; body=b; next=x;}
	public FunctionDec(int p, Symbol n, FieldList a, NameTy r, Exp b, FunctionDec x, boolean ca, int o)
	{pos=p; name=n; params=a; result=r; body=b; next=x;call = ca;outGoing = o;}

	public void append(FunctionDec t)
	{
		if(next == null)next = t;
		else next.append(t);
	}
	public FunctionDec clone(){
		FieldList p = null;
		NameTy r = null;
		FunctionDec n = null;
		if(params != null)p = params.clone();
		if(result != null)r = result.clone();
		if(next != null)n = next.clone();
		return new FunctionDec(pos, name, p, r, body.clone(), n, call, outGoing);
	}
}
