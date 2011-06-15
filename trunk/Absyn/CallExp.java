package Absyn;
import Symbol.Symbol;
public class CallExp extends Exp {
   public Symbol func;
   public ExpList args;
   public CallExp(int p, Symbol f, ExpList a) {pos=p; func=f; args=a;}
	public CallExp clone(){
		return new CallExp(pos, func, args.clone());
	}
}
