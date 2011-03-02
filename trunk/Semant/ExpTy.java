package Semant;
import Translate.Exp;
public class ExpTy
{
	public Exp exp;
	public Types.Type ty;
	ExpTy(Exp e, Types.Type t){exp = e; ty = t;}
}