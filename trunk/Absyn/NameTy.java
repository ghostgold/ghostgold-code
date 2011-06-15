package Absyn;
import Symbol.Symbol;
public class NameTy extends Ty {
   public Symbol name;
   public NameTy(int p, Symbol n) {pos=p; name=n;}
	public NameTy clone(){
		return new NameTy(pos, name);
	}
}
