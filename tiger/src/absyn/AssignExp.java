package absyn;
import symbol.Symbol;
public class AssignExp extends Exp {
   public Var var;
   public Exp exp;
   public AssignExp(int p, Var v, Exp e) {pos=p; var=v; exp=e;}
	public AssignExp clone(){
		return new AssignExp(pos, var.clone(), exp.clone());
	}
}
