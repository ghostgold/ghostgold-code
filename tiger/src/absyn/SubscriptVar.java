package absyn;
import symbol.Symbol;
public class SubscriptVar extends Var {
   public Var var;
   public Exp index;
   public SubscriptVar(int p, Var v, Exp i) {pos=p; var=v; index=i;}
	public SubscriptVar clone(){
		return new SubscriptVar(pos, var.clone(), index.clone());
	}
}
