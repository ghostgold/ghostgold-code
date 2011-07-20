package absyn;
import symbol.Symbol;
public class ArrayExp extends Exp {
   public Symbol typ;
   public Exp size, init;
   public ArrayExp(int p, Symbol t, Exp s, Exp i) {pos=p; typ=t; size=s; init=i;}
	public ArrayExp clone(){
		return new ArrayExp(pos, typ, size.clone(), init.clone());
	}
}
