package absyn;
import symbol.Symbol;
public class ArrayTy extends Ty {
   public Symbol typ;
   public ArrayTy(int p, Symbol t) {pos=p; typ=t;}
	public ArrayTy clone(){
		return new ArrayTy(pos, typ);
	}
}
