package absyn;
import symbol.Symbol;
public class FieldVar extends Var {
   public Var var;
   public Symbol field;
   public FieldVar(int p, Var v, Symbol f) {pos=p; var=v; field=f;}
	public FieldVar clone(){
		return new FieldVar(pos, var.clone(), field);
	}
}
