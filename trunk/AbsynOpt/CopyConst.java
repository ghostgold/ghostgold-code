package AbsynOpt;
import Absyn.*;
import java.util.*;
import Semant.Env;

public class CopyConst
{
	public static Symbol.Table varenv = new Symbol.Table();
	public static  Exp copyConstExp(Exp e){
		if(e instanceof VarExp)return copyConstExp((VarExp)e);
		else if(e instanceof NilExp)return copyConstExp((NilExp)e);
		else if(e instanceof IntExp)return copyConstExp((IntExp)e);
		else if(e instanceof StringExp)return copyConstExp((StringExp)e);
		else if(e instanceof CallExp)return copyConstExp((CallExp)e);
		else if(e instanceof OpExp)return copyConstExp((OpExp)e);
		else if(e instanceof RecordExp)return copyConstExp((RecordExp)e);
		else if(e instanceof SeqExp)return copyConstExp((SeqExp)e);
		else if(e instanceof AssignExp)return copyConstExp((AssignExp)e);
		else if(e instanceof IfExp)return copyConstExp((IfExp)e);
		else if(e instanceof WhileExp)return copyConstExp((WhileExp)e);
		else if(e instanceof ForExp)return copyConstExp((ForExp)e);
		else if(e instanceof BreakExp)return copyConstExp((BreakExp)e);
		else if(e instanceof LetExp)return copyConstExp((LetExp)e);
		else if(e instanceof ArrayExp)return copyConstExp((ArrayExp)e);
		else throw new Error("copyConstExp");
	}
	public static  Exp copyConstExp(VarExp e){
		if(e.var instanceof SimpleVar){
			IntExp init = (IntExp)varenv.get(((SimpleVar)e.var).name);
			if(init != null){
				return init;
			}
		}
		return e;
	}
	public static  NilExp copyConstExp(NilExp e){
		return e;
	}
	public static  IntExp copyConstExp(IntExp e){
		return e;
	}
	public static  StringExp copyConstExp(StringExp e){
		return e;
	}
	public static  Exp copyConstExp(CallExp call){
		ExpList args = call.args;
		while(args != null){
			args.head = copyConstExp(args.head);
			args = args.tail;
		}
		return call;
	}
	public static  Exp copyConstExp(OpExp e){
		e.left = copyConstExp(e.left);
		e.right = copyConstExp(e.right);
		if(e.left instanceof IntExp && e.right instanceof IntExp){
			switch(e.oper){
			case OpExp.PLUS:
				return new IntExp(0, ((IntExp)e.left).value + ((IntExp)e.right).value);
			case OpExp.MINUS:
				return new IntExp(0, ((IntExp)e.left).value - ((IntExp)e.right).value);
			case OpExp.MUL:
				return new IntExp(0, ((IntExp)e.left).value * ((IntExp)e.right).value);
			}
		}
		return e;
	}
	public static  RecordExp copyConstExp(RecordExp e){
		FieldExpList fields = e.fields;
		while (fields != null){
			fields.init = copyConstExp(fields.init);
			fields = fields.tail;
		}
		return e;
	}
	public static  SeqExp copyConstExp(SeqExp e){
		ExpList list = e.list;
		while (list != null){
			list.head = copyConstExp(list.head);
			list = list.tail;
		}
		return e;
	}
	public static  AssignExp copyConstExp(AssignExp e){
		e.var = copyConstVar(e.var);
		e.exp = copyConstExp(e.exp);
		return e;
	}
	public static Var copyConstVar(Var v){
		if(v instanceof SimpleVar)return v;
		else if (v instanceof FieldVar)return copyConstVar((FieldVar)v);
		else if (v instanceof SubscriptVar)return copyConstVar((SubscriptVar)v);
		throw new Error("copyConstVar");
	}
	public static Var copyConstVar(FieldVar v){
		v.var = copyConstVar(v.var);
		return v;
	}
	public static Var copyConstVar(SubscriptVar v ){
		v.var = copyConstVar(v.var);
		v.index = copyConstExp(v.index);
		return v;
	}
	public static  IfExp copyConstExp(IfExp e){
		e.test = copyConstExp(e.test);
		e.thenclause = copyConstExp(e.thenclause);
		if (e.elseclause != null)
			e.elseclause = copyConstExp(e.elseclause);
		return e;
	}
	public static  WhileExp copyConstExp(WhileExp e){
		e.test = copyConstExp(e.test);
		e.body = copyConstExp(e.body);
		return e;
	}
	public static  ForExp copyConstExp(ForExp e){
		e.var = copyConstDec(e.var);
		e.hi = copyConstExp(e.hi);
		e.body = copyConstExp(e.body);
		return e;
	}
	public static  BreakExp copyConstExp(BreakExp e){
		return e;
	}
	public static  Exp copyConstExp(LetExp e){
		DecList decs = e.decs;
		varenv.beginScope();
		while (decs != null){
			decs.head = copyConstDec(decs.head);
			decs = decs.tail;
		}
		e.body = copyConstExp(e.body);
		varenv.endScope();
		return e;
	}
	public static  ArrayExp copyConstExp(ArrayExp e){
		e.size = copyConstExp(e.size);
		e.init = copyConstExp(e.init);
		return e;
	}
	
	public static  Dec copyConstDec(Dec d){
		if(d instanceof VarDec)return copyConstDec((VarDec)d);
		else if(d instanceof FunctionDec)return copyConstDec((FunctionDec)d);
		else if(d instanceof TypeDec)return copyConstDec((TypeDec)d);
		else throw new Error("copyConstDec");
	}
	
	public static  VarDec copyConstDec(VarDec d){
		d.init = copyConstExp(d.init);
		if(d.init instanceof IntExp && d.isConst){
			varenv.put(d.name, d.init);
		}
		return d;
	}

	public static FunctionDec copyConstDec(FunctionDec d){
		FunctionDec dd = d;
		while(dd != null){
			varenv.beginScope();
			dd.body = copyConstExp(dd.body);
			varenv.endScope();
			dd = dd.next;
		}
		return d;
	}
	
	public static TypeDec copyConstDec(TypeDec d){
		return d;
	}

}