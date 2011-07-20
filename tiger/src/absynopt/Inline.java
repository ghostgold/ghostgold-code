package absynopt;
import java.util.*;

import semant.Env;

import absyn.*;
public class Inline
{
	public static symbol.Table funcenv = new symbol.Table();
	public static  Exp inlineExp(Exp e){
		if(e instanceof VarExp)return inlineExp((VarExp)e);
		else if(e instanceof NilExp)return inlineExp((NilExp)e);
		else if(e instanceof IntExp)return inlineExp((IntExp)e);
		else if(e instanceof StringExp)return inlineExp((StringExp)e);
		else if(e instanceof CallExp)return inlineExp((CallExp)e);
		else if(e instanceof OpExp)return inlineExp((OpExp)e);
		else if(e instanceof RecordExp)return inlineExp((RecordExp)e);
		else if(e instanceof SeqExp)return inlineExp((SeqExp)e);
		else if(e instanceof AssignExp)return inlineExp((AssignExp)e);
		else if(e instanceof IfExp)return inlineExp((IfExp)e);
		else if(e instanceof WhileExp)return inlineExp((WhileExp)e);
		else if(e instanceof ForExp)return inlineExp((ForExp)e);
		else if(e instanceof BreakExp)return inlineExp((BreakExp)e);
		else if(e instanceof LetExp)return inlineExp((LetExp)e);
		else if(e instanceof ArrayExp)return inlineExp((ArrayExp)e);
		else throw new Error("inlineExp");
	}
	public static  VarExp inlineExp(VarExp e){
		return e;
	}
	public static  NilExp inlineExp(NilExp e){
		return e;
	}
	public static  IntExp inlineExp(IntExp e){
		return e;
	}
	public static  StringExp inlineExp(StringExp e){
		return e;
	}
	public static  Exp inlineExp(CallExp call){
		FunctionDec f = (FunctionDec)funcenv.get(call.func);
		if(f != null && f.call == false){
			FunctionDec func = f.clone();
			/*			ReNaming.env = new Semant.Env();
			ReNaming rename = new ReNaming(func);
			func = rename.renameDecOnlyOne(func);*/
			
			ExpList args = call.args;
			FieldList params = func.params;
			if(args == null && params == null)
				return func.body;
			else if(args != null && params != null){
				DecList decs = new DecList(new VarDec(0, params.name, new NameTy(0, params.typ), inlineExp(args.head)), null);
				args = args.tail;
				params = params.tail;
				DecList loopdecs = decs;
				while(args != null && params != null){
					loopdecs.tail = new DecList(new VarDec(0, params.name, new NameTy(0, params.typ), inlineExp(args.head)), null);
					loopdecs = loopdecs.tail;
					args = args.tail;
					params = params.tail;
				}
				if(args != null || params != null)
					throw new Error("args number error");
				else {
					return new LetExp(0, decs, func.body.clone());
				}
			}
		}
		else {
			ExpList args = call.args;
			while(args != null){
				args.head = inlineExp(args.head);
				args = args.tail;
			}
			return call;
		}
		return call;
	}
	/*	public static Exp inlineCall(CallExp call){
		FunctionDec func = (FunctionDec)funcenv.get(call.func);
		if(func.call == false){
			ExpList argslist = call.args;
			FieldList paramslist = func.params;
			if(argslist == null && paramslist == null)
				args.head = func.body;
			else if(argslist != null && paramslist != null){
				DecList decs = new DecList(new VarDec(0, paramslist.head.name, null, argslist.head), null);//one level?
				argslist = argslist.tail;
				paramslist = paramslist.tail;
				DecList loopdecs = decs;
				while(argslist != null && paramslist != null){
					loopdecs.tail = new DecList(new VarDec(0, paramslist.head.name, null, argslist.head), null);
					loopdecs = loopdecs.tail;
				}
				if(argslist != null || paramslist != null)
					throw new Error("args number error");
				else {
					args.head = new LetExp(0, decs, func.body);
				}
			}
		}
		else return call;
		}*/
	public static  OpExp inlineExp(OpExp e){
		e.left = inlineExp(e.left);
		e.right = inlineExp(e.right);
		return e;
	}
	public static  RecordExp inlineExp(RecordExp e){
		FieldExpList fields = e.fields;
		while (fields != null){
			fields.init = inlineExp(fields.init);
			fields = fields.tail;
		}
		return e;
	}
	public static  SeqExp inlineExp(SeqExp e){
		ExpList list = e.list;
		while (list != null){
			list.head = inlineExp(list.head);
			list = list.tail;
		}
		return e;
	}
	public static  AssignExp inlineExp(AssignExp e){
		e.exp = inlineExp(e.exp);
		return e;
	}
	public static  IfExp inlineExp(IfExp e){
		e.test = inlineExp(e.test);
		e.thenclause = inlineExp(e.thenclause);
		if (e.elseclause != null)
			e.elseclause = inlineExp(e.elseclause);
		return e;
	}
	public static  WhileExp inlineExp(WhileExp e){
		e.test = inlineExp(e.test);
		e.body = inlineExp(e.body);
		return e;
	}
	public static  ForExp inlineExp(ForExp e){
		e.var = inlineDec(e.var);
		e.hi = inlineExp(e.hi);
		e.body = inlineExp(e.body);
		return e;
	}
	public static  BreakExp inlineExp(BreakExp e){
		return e;
	}
	public static  Exp inlineExp(LetExp e){
		DecList decs = e.decs;
		funcenv.beginScope();
		while (decs != null){
			decs.head = inlineDec(decs.head);
			decs = decs.tail;
		}
		while(e.decs != null && e.decs.head  == null)e.decs = e.decs.tail;
		decs = e.decs;
		while(decs!= null && decs.tail != null){
			while(decs.tail != null && decs.tail.head == null)
				decs.tail = decs.tail.tail;
			decs = decs.tail;
		}
		e.body = inlineExp(e.body);
		funcenv.endScope();
		if(e.decs == null)return e.body;
		return e;
	}
	public static  ArrayExp inlineExp(ArrayExp e){
		e.size = inlineExp(e.size);
		e.init = inlineExp(e.init);
		return e;
	}
	
	public static  Dec inlineDec(Dec d){
		if(d instanceof VarDec)return inlineDec((VarDec)d);
		else if(d instanceof FunctionDec)return inlineDec((FunctionDec)d);
		else if(d instanceof TypeDec)return inlineDec((TypeDec)d);
		else throw new Error("inlineDec");
	}
	
	public static  VarDec inlineDec(VarDec d){
		d.init = inlineExp(d.init);
		return d;
	}

	public static FunctionDec inlineDec(FunctionDec d){
		FunctionDec dd = d;
		while(dd != null){
			funcenv.put(dd.name, dd);
			dd = dd.next;
		}
		dd = d;
		while(dd != null){
			funcenv.beginScope();
			dd.body = inlineExp(dd.body);
			funcenv.endScope();
			dd = dd.next;
		}
		dd = d;
		while(dd != null && dd.call == false)dd = dd.next;
		d = dd;
		if(dd == null)return null;
		while(dd!= null && dd.next != null){
			while(dd.next != null && dd.next.call == false)
				dd.next = dd.next.next;
			dd = dd.next;
		}
		return d;
	}
	
	public static TypeDec inlineDec(TypeDec d){
		return d;
	}

}