package semant;
import translate.Exp;
public class ExpTy
{
	public Exp exp;
	public types.Type ty;
	ExpTy(Exp e, types.Type t){exp = e; ty = t;}
}