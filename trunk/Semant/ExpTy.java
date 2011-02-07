package Semant;
import Translate.Exp;
class ExpTy
{
	Exp exp;
	Types.Type ty;
	ExpTy(Exp e, Types.Type t){exp = e; ty = t;}
}