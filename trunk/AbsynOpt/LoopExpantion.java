package AbsynOpt;
import Absyn.*;
import java.util.*;
import Semant.Env;
public class LoopExpantion
{
	public static  Exp loopExpantion(Exp e){
		if(e instanceof VarExp)return loopExpantion((VarExp)e);
		else if(e instanceof NilExp)return loopExpantion((NilExp)e);
		else if(e instanceof IntExp)return loopExpantion((IntExp)e);
		else if(e instanceof StringExp)return loopExpantion((StringExp)e);
		else if(e instanceof CallExp)return loopExpantion((CallExp)e);
		else if(e instanceof OpExp)return loopExpantion((OpExp)e);
		else if(e instanceof RecordExp)return loopExpantion((RecordExp)e);
		else if(e instanceof SeqExp)return loopExpantion((SeqExp)e);
		else if(e instanceof AssignExp)return loopExpantion((AssignExp)e);
		else if(e instanceof IfExp)return loopExpantion((IfExp)e);
		else if(e instanceof WhileExp)return loopExpantion((WhileExp)e);
		else if(e instanceof ForExp)return loopExpantion((ForExp)e);
		else if(e instanceof BreakExp)return loopExpantion((BreakExp)e);
		else if(e instanceof LetExp)return loopExpantion((LetExp)e);
		else if(e instanceof ArrayExp)return loopExpantion((ArrayExp)e);
		else throw new Error("loopExpantion");
	}
	public static  VarExp loopExpantion(VarExp e){
		return e;
	}
	public static  NilExp loopExpantion(NilExp e){
		return e;
	}
	public static  IntExp loopExpantion(IntExp e){
		return e;
	}
	public static  StringExp loopExpantion(StringExp e){
		return e;
	}
	public static  Exp loopExpantion(CallExp call){
		ExpList args = call.args;
		while(args != null){
			args.head = loopExpantion(args.head);
			args = args.tail;
		}
		return call;
	}
	public static  OpExp loopExpantion(OpExp e){
		e.left = loopExpantion(e.left);
		e.right = loopExpantion(e.right);
		return e;
	}

	public static  RecordExp loopExpantion(RecordExp e){
		FieldExpList fields = e.fields;
		while (fields != null){
			fields.init = loopExpantion(fields.init);
			fields = fields.tail;
		}
		return e;
	}
	public static  SeqExp loopExpantion(SeqExp e){
		ExpList list = e.list;
		while (list != null){
			list.head = loopExpantion(list.head);
			list = list.tail;
		}
		return e;
	}
	public static  AssignExp loopExpantion(AssignExp e){
		e.exp = loopExpantion(e.exp);
		return e;
	}
	public static  IfExp loopExpantion(IfExp e){
		e.test = loopExpantion(e.test);
		e.thenclause = loopExpantion(e.thenclause);
		if (e.elseclause != null)
			e.elseclause = loopExpantion(e.elseclause);
		return e;
	}
	public static  WhileExp loopExpantion(WhileExp e){
		e.test = loopExpantion(e.test);
		e.body = loopExpantion(e.body);
		return e;
	}
	public static  Exp loopExpantion(ForExp e){
		VarDec var = e.var;
		if(var.init instanceof IntExp && e.hi instanceof IntExp){
			int hi = ((IntExp)e.hi).value;
			int low = ((IntExp)var.init).value;
			ExpList list = null;
			if(hi - low <= 10 && hi - low >= 0){
				for(int i = hi; i >= low; i--){
					list = new ExpList(e.body.clone(), list);
					list.head = copyConst(list.head, var.name, i);
				}
				if(list == null)return new NilExp(0);
				else return new SeqExp(e.pos, list);
			}
		}
		e.var = loopExpantion(e.var);
		e.hi = loopExpantion(e.hi);
		e.body = loopExpantion(e.body);
		return e;
	}
	public static  BreakExp loopExpantion(BreakExp e){
		return e;
	}
	public static  Exp loopExpantion(LetExp e){
		DecList decs = e.decs;
		while (decs != null){
			decs.head = loopExpantion(decs.head);
			decs = decs.tail;
		}
		e.body = loopExpantion(e.body);
		return e;
	}
	public static  ArrayExp loopExpantion(ArrayExp e){
		e.size = loopExpantion(e.size);
		e.init = loopExpantion(e.init);
		return e;
	}
	
	public static  Dec loopExpantion(Dec d){
		if(d instanceof VarDec)return loopExpantion((VarDec)d);
		else if(d instanceof FunctionDec)return loopExpantion((FunctionDec)d);
		else if(d instanceof TypeDec)return loopExpantion((TypeDec)d);
		else throw new Error("loopExpantion");
	}
	
	public static  VarDec loopExpantion(VarDec d){
		d.init = loopExpantion(d.init);
		return d;
	}

	public static FunctionDec loopExpantion(FunctionDec d){
		FunctionDec dd = d;
		while(dd != null){
			dd.body = loopExpantion(dd.body);
			dd = dd.next;
		}
		return d;
	}
	public static TypeDec loopExpantion(TypeDec d){
		return d;
	}
	public static  Exp copyConst(Exp e, Symbol.Symbol name, int c){
		if(e instanceof VarExp)return copyConst((VarExp)e, name, c);
		else if(e instanceof NilExp)return copyConst((NilExp)e, name, c);
		else if(e instanceof IntExp)return copyConst((IntExp)e, name, c);
		else if(e instanceof StringExp)return copyConst((StringExp)e, name, c);
		else if(e instanceof CallExp)return copyConst((CallExp)e, name, c);
		else if(e instanceof OpExp)return copyConst((OpExp)e, name, c);
		else if(e instanceof RecordExp)return copyConst((RecordExp)e, name, c);
		else if(e instanceof SeqExp)return copyConst((SeqExp)e, name, c);
		else if(e instanceof AssignExp)return copyConst((AssignExp)e, name, c);
		else if(e instanceof IfExp)return copyConst((IfExp)e, name, c);
		else if(e instanceof WhileExp)return copyConst((WhileExp)e, name, c);
		else if(e instanceof ForExp)return copyConst((ForExp)e, name, c);
		else if(e instanceof BreakExp)return copyConst((BreakExp)e, name, c);
		else if(e instanceof LetExp)return copyConst((LetExp)e, name, c);
		else if(e instanceof ArrayExp)return copyConst((ArrayExp)e, name, c);
		else throw new Error("copyConst");
	}
	public static  Exp copyConst(VarExp e,Symbol.Symbol name, int c){
		if(e.var instanceof SimpleVar){
			SimpleVar var = (SimpleVar)e.var;
			if(var.name == name){
				return new IntExp(0,c);
			}
		}
		e.var = copyConst(e.var, name, c);
		return e;
	}
	public static Var copyConst(Var v, Symbol.Symbol name, int c){
		if(v instanceof SimpleVar)return v;
		else if(v instanceof FieldVar)return copyConst((FieldVar)v, name, c);
		else if(v instanceof SubscriptVar)return copyConst((SubscriptVar)v, name, c);
		throw new Error("copyConstVar");
	}
	public static Var copyConst(FieldVar v, Symbol.Symbol name, int c){
		v.var = copyConst(v.var, name, c);
		return v;
	}
	public static Var copyConst(SubscriptVar v, Symbol.Symbol name, int c){
		v.var = copyConst(v.var, name, c);
		v.index = copyConst(v.index, name, c);
		return v;
	}
	public static  NilExp copyConst(NilExp e, Symbol.Symbol name, int c){
		return e;
	}
	public static  IntExp copyConst(IntExp e, Symbol.Symbol name, int c){
		return e;
	}
	public static  StringExp copyConst(StringExp e, Symbol.Symbol name, int c){
		return e;
	}
	public static  Exp copyConst(CallExp call, Symbol.Symbol name, int c){
		ExpList args = call.args;
		while(args != null){
			args.head = copyConst(args.head, name, c);
			args = args.tail;
		}
		return call;
	}
	public static  Exp copyConst(OpExp e, Symbol.Symbol name, int c){
		e.left = copyConst(e.left, name, c);
		e.right = copyConst(e.right, name, c);
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

	public static  RecordExp copyConst(RecordExp e, Symbol.Symbol name, int c){
		FieldExpList fields = e.fields;
		while (fields != null){
			fields.init = copyConst(fields.init, name, c);
			fields = fields.tail;
		}
		return e;
	}
	public static  SeqExp copyConst(SeqExp e, Symbol.Symbol name, int c){
		ExpList list = e.list;
		while (list != null){
			list.head = copyConst(list.head, name, c);
			list = list.tail;
		}
		return e;
	}
	public static  AssignExp copyConst(AssignExp e, Symbol.Symbol name, int c){
		e.var = copyConst(e.var, name, c);
		e.exp = copyConst(e.exp, name, c);
		return e;
	}
	public static  IfExp copyConst(IfExp e, Symbol.Symbol name, int c){
		e.test = copyConst(e.test, name, c);
		e.thenclause = copyConst(e.thenclause, name, c);
		if (e.elseclause != null)
			e.elseclause = copyConst(e.elseclause, name, c);
		return e;
	}
	public static  WhileExp copyConst(WhileExp e, Symbol.Symbol name, int c){
		e.test = copyConst(e.test, name, c);
		e.body = copyConst(e.body, name, c);
		return e;
	}
	public static  Exp copyConst(ForExp e, Symbol.Symbol name, int c){
		e.var = copyConst(e.var, name, c);
		e.hi = copyConst(e.hi, name, c);
		e.body = copyConst(e.body, name, c);
		return e;
	}
	public static  BreakExp copyConst(BreakExp e, Symbol.Symbol name, int c){
		return e;
	}
	public static  Exp copyConst(LetExp e, Symbol.Symbol name, int c){
		DecList decs = e.decs;
		while (decs != null){
			decs.head = copyConst(decs.head, name, c);
			decs = decs.tail;
		}
		e.body = copyConst(e.body, name, c);
		return e;
	}
	public static  ArrayExp copyConst(ArrayExp e, Symbol.Symbol name, int c){
		e.size = copyConst(e.size, name, c);
		e.init = copyConst(e.init, name, c);
		return e;
	}
	
	public static  Dec copyConst(Dec d, Symbol.Symbol name, int c){
		if(d instanceof VarDec)return copyConst((VarDec)d, name, c);
		else if(d instanceof FunctionDec)return copyConst((FunctionDec)d, name, c);
		else if(d instanceof TypeDec)return copyConst((TypeDec)d, name, c);
		else throw new Error("copyConst");
	}
	
	public static  VarDec copyConst(VarDec d, Symbol.Symbol name, int c){
		d.init = copyConst(d.init, name, c);
		return d;
	}

	public static FunctionDec copyConst(FunctionDec d, Symbol.Symbol name, int c){
		FunctionDec dd = d;
		while(dd != null){
			dd.body = copyConst(dd.body, name, c);
			dd = dd.next;
		}
		return d;
	}
	public static TypeDec copyConst(TypeDec d, Symbol.Symbol name, int c){
		return d;
	}

}