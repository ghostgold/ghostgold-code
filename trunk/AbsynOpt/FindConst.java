package AbsynOpt;
import Absyn.*;
import java.util.*;
import Semant.Env;

abstract class Entry{
	abstract void setNotConst();
}

class FormalEntry extends Entry{
	FieldList fl;
	FormalEntry(FieldList f){
		fl = f;
		fl.isConst = true;
	}
	void setNotConst(){fl.isConst = false;}
}

class VarEntry extends Entry{
	VarDec vd;
	VarEntry(VarDec v){
		vd = v;
		vd.isConst = true;
	}
	void setNotConst(){vd.isConst = false;}
}

public class FindConst
{
	public static Symbol.Table varenv = new Symbol.Table();
	public static  void findConstExp(Exp e){
		if(e instanceof VarExp) findConstExp((VarExp)e);
		else if(e instanceof NilExp) findConstExp((NilExp)e);
		else if(e instanceof IntExp) findConstExp((IntExp)e);
		else if(e instanceof StringExp) findConstExp((StringExp)e);
		else if(e instanceof CallExp) findConstExp((CallExp)e);
		else if(e instanceof OpExp) findConstExp((OpExp)e);
		else if(e instanceof RecordExp) findConstExp((RecordExp)e);
		else if(e instanceof SeqExp) findConstExp((SeqExp)e);
		else if(e instanceof AssignExp) findConstExp((AssignExp)e);
		else if(e instanceof IfExp) findConstExp((IfExp)e);
		else if(e instanceof WhileExp) findConstExp((WhileExp)e);
		else if(e instanceof ForExp) findConstExp((ForExp)e);
		else if(e instanceof BreakExp) findConstExp((BreakExp)e);
		else if(e instanceof LetExp) findConstExp((LetExp)e);
		else if(e instanceof ArrayExp) findConstExp((ArrayExp)e);
		else throw new Error("findConstExp");
	}
	public static  void findConstExp(VarExp e){
	}
	public static  void findConstExp(NilExp e){
	}
	public static  void findConstExp(IntExp e){
	}
	public static  void findConstExp(StringExp e){
	}
	public static  void findConstExp(CallExp call){
		ExpList args = call.args;
		while (args != null){
			findConstExp(args.head);
			args = args.tail;
		}
	}

	public static  void findConstExp(OpExp e){
		findConstExp(e.left);
		findConstExp(e.right);
	}
	public static  void findConstExp(RecordExp e){
		FieldExpList fields = e.fields;
		while (fields != null){
			findConstExp(fields.init);
			fields = fields.tail;
		}
	}
	public static void  findConstExp(SeqExp e){
		ExpList list = e.list;
		while (list != null){
			findConstExp(list.head);
			list = list.tail;
		}
	}
	public static void   findConstExp(AssignExp e){
		if(e.var instanceof SimpleVar){
			Entry var = (Entry)varenv.get(((SimpleVar)e.var).name);
			if(var != null)var.setNotConst();
		}
		findConstExp(e.exp);
	}
	public static void   findConstExp(IfExp e){
		findConstExp(e.test);
		findConstExp(e.thenclause);
		if (e.elseclause != null)
			findConstExp(e.elseclause);
	}
	public static void   findConstExp(WhileExp e){
		findConstExp(e.test);
		findConstExp(e.body);
	}
	public static void   findConstExp(ForExp e){
		VarEntry var = new VarEntry(e.var);
		var.setNotConst();
		findConstExp(e.var.init);
		findConstExp(e.hi);
		varenv.beginScope();
		varenv.put(e.var.name, var);
		findConstExp(e.body);
		varenv.endScope();
	}
	public static void   findConstExp(BreakExp e){
	}
	public static void   findConstExp(LetExp e){
		DecList decs = e.decs;
		varenv.beginScope();
		while (decs != null){
			findConstDec(decs.head);
			decs = decs.tail;
		}
		findConstExp(e.body);
		varenv.endScope();
	}
	public static void   findConstExp(ArrayExp e){
		findConstExp(e.size);
		findConstExp(e.init);
	}
	public static void findConstVar(Var v){
		if(v instanceof SimpleVar)return;
		else if(v instanceof FieldVar)findConstVar((FieldVar)v);
		else if(v instanceof SubscriptVar)findConstVar((SubscriptVar)v);
	}

	public static void findConstVar(SubscriptVar v){
		findConstVar(v.var);
		findConstExp(v.index);
	}

	public static void findConstVar(FieldVar v){
		findConstVar(v.var);
	}

	public static void findConstDec(Dec d){
		if(d instanceof VarDec) findConstDec((VarDec)d);
		else if(d instanceof FunctionDec) findConstDec((FunctionDec)d);
		else if(d instanceof TypeDec) findConstDec((TypeDec)d);
		else throw new Error("findConstDec");
	}
	
	public static  void findConstDec(VarDec d){
		VarEntry var = new VarEntry(d);
		varenv.put(d.name, var);
		findConstExp(d.init);
	}

	public static void findConstDec(FunctionDec d){
		while(d != null){
			FieldList params = d.params;
			varenv.beginScope();
			while(params != null){
				varenv.put(params.name, new FormalEntry(params));
				params = params.tail;
			}
			findConstExp(d.body);
			varenv.endScope();
			d = d.next;
		}
	}
	
	public static void findConstDec(TypeDec d){

	}

}