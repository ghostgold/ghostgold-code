package absyn;
import symbol.Symbol;
public class SimpleVar extends Var {
   public Symbol name;
   public SimpleVar (int p, Symbol n) {pos=p; name=n;}
	public SimpleVar clone(){
		return new SimpleVar(pos, name);
	}

}
