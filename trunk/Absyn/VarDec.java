package Absyn;
import Symbol.Symbol;
public class VarDec extends Dec {
   public Symbol name;
   public boolean escape = true;
   public NameTy typ; /* optional */
   public Exp init;
   public boolean isConst = true;
   public VarDec(int p, Symbol n, NameTy t, Exp i) {pos=p; name=n; typ=t; init=i;}
	public VarDec(int p, Symbol n, NameTy t, Exp i, boolean esc, boolean isCon) {pos=p; name=n; typ=t; init=i;escape = esc; isConst = isCon;}

	public VarDec clone(){
		if(typ != null)return new VarDec(pos, name, typ.clone(), init.clone(), escape, isConst);
		else return new VarDec(pos, name, null, init.clone(), escape, isConst);
	}
}
