package AbsynOpt;
import Absyn.*;
import java.util.*;
import Semant.Env;
public class ReNaming
{
	public static Env env = new Env();
	public static int count = 0;
	public FunctionDec home;
	public ReNaming(FunctionDec f){
		home = f;
		if(home == null){
			home = new FunctionDec(0, null, null, null, null, null);
		}
	}
	public  Exp renameExp(Exp e){
		if(e instanceof VarExp)return renameExp((VarExp)e);
		else if(e instanceof NilExp)return renameExp((NilExp)e);
		else if(e instanceof IntExp)return renameExp((IntExp)e);
		else if(e instanceof StringExp)return renameExp((StringExp)e);
		else if(e instanceof CallExp)return renameExp((CallExp)e);
		else if(e instanceof OpExp)return renameExp((OpExp)e);
		else if(e instanceof RecordExp)return renameExp((RecordExp)e);
		else if(e instanceof SeqExp)return renameExp((SeqExp)e);
		else if(e instanceof AssignExp)return renameExp((AssignExp)e);
		else if(e instanceof IfExp)return renameExp((IfExp)e);
		else if(e instanceof WhileExp)return renameExp((WhileExp)e);
		else if(e instanceof ForExp)return renameExp((ForExp)e);
		else if(e instanceof BreakExp)return renameExp((BreakExp)e);
		else if(e instanceof LetExp)return renameExp((LetExp)e);
		else if(e instanceof ArrayExp)return renameExp((ArrayExp)e);
		else throw new Error("renameExp");
	}
	public  VarExp renameExp(VarExp e){
		e.var = renameVar(e.var);
		return e;
	}
	public  NilExp renameExp(NilExp e){
		return e;
	}
	public  IntExp renameExp(IntExp e){
		return e;
	}
	public  StringExp renameExp(StringExp e){
		return e;
	}
	public  CallExp renameExp(CallExp e){
		Symbol.Symbol func = (Symbol.Symbol)env.venv.get(e.func);
		if (func != null){
			e.func = func;
			home.call = true;
		}
		ExpList args = e.args;
		while (args != null){
			args.head = renameExp(args.head);
			args = args.tail;
		}
		return e;
	}
	public  OpExp renameExp(OpExp e){
		e.left = renameExp(e.left);
		e.right = renameExp(e.right);
		return e;
	}
	public  RecordExp renameExp(RecordExp e){
		Symbol.Symbol typ = (Symbol.Symbol)env.tenv.get(e.typ);
		if (typ != null)
			e.typ = typ;
		FieldExpList fields = e.fields;
		while (fields != null){
			fields.init = renameExp(fields.init);
			fields = fields.tail;
		}
		return e;
	}
	public  SeqExp renameExp(SeqExp e){
		ExpList list = e.list;
		while (list != null){
			list.head = renameExp(list.head);
			list = list.tail;
		}
		return e;
	}
	public  AssignExp renameExp(AssignExp e){
		e.var = renameVar(e.var);
		e.exp = renameExp(e.exp);
		return e;
	}
	public  IfExp renameExp(IfExp e){
		e.test = renameExp(e.test);
		e.thenclause = renameExp(e.thenclause);
		if (e.elseclause != null)
			e.elseclause = renameExp(e.elseclause);
		return e;
	}
	public  WhileExp renameExp(WhileExp e){
		e.test = renameExp(e.test);
		e.body = renameExp(e.body);
		return e;
	}
	public  ForExp renameExp(ForExp e){
		env.venv.beginScope();
		e.var = renameDec(e.var);
		e.hi = renameExp(e.hi);
		e.body = renameExp(e.body);
		env.venv.endScope();
		return e;
	}
	public  BreakExp renameExp(BreakExp e){
		return e;
	}
	public  LetExp renameExp(LetExp e){
		DecList decs = e.decs;
		env.tenv.beginScope();
		env.venv.beginScope();
		while (decs != null){
			decs.head = renameDec(decs.head);
			decs = decs.tail;
		}
		e.body = renameExp(e.body);
		env.tenv.endScope();
		env.venv.endScope();
		return e;
	}
	public  ArrayExp renameExp(ArrayExp e){
		Symbol.Symbol typ = (Symbol.Symbol)env.tenv.get(e.typ);
		if (typ != null)
			e.typ = typ;
		e.size = renameExp(e.size);
		e.init = renameExp(e.init);
		return e;
	}
	public  Var renameVar(Var v){
		if (v instanceof SimpleVar)return renameVar((SimpleVar)v);
		else if (v instanceof FieldVar)return renameVar((FieldVar)v);
		else if (v instanceof SubscriptVar)return renameVar((SubscriptVar)v);
		throw new Error("renameVar");
	}
		
	public  SimpleVar renameVar(SimpleVar v){
		Symbol.Symbol name = (Symbol.Symbol)env.venv.get(v.name);
		if (name != null)
			v.name = name;
		return v;
	}
	
	public  FieldVar renameVar(FieldVar v){
		v.var = renameVar(v.var);
		return v;
	}
	
	public  SubscriptVar renameVar(SubscriptVar v){
		v.var = renameVar(v.var);
		v.index = renameExp(v.index);
		return v;
	}
	
	public  Dec renameDec(Dec d){
		if(d instanceof VarDec)return renameDec((VarDec)d);
		else if(d instanceof FunctionDec)return renameDec((FunctionDec)d);
		else if(d instanceof TypeDec)return renameDec((TypeDec)d);
		else throw new Error("renameDec");
	}
	
	public  VarDec renameDec(VarDec d){
		d.init = renameExp(d.init);
		Symbol.Symbol newName = Symbol.Symbol.symbol(d.name.toString() + "_" + count);
		count ++;
		env.venv.put(d.name, newName);
		d.name = newName;
		if(d.typ != null)
			d.typ = renameTy(d.typ);
		return d;
	}

	public FunctionDec renameDec(FunctionDec d){
		FunctionDec dd = d;
		while(dd != null){
			Symbol.Symbol newName = Symbol.Symbol.symbol(dd.name.toString() + "_" + count);
			count++;
			env.venv.put(dd.name, newName);
			dd.name = newName;
			dd = dd.next;
		}
		dd = d;
		while(dd != null){
			if(dd.result != null)
				dd.result = renameTy(dd.result);
			env.tenv.beginScope();
			env.venv.beginScope();
			FieldList params = dd.params;
			while(params != null){
				Symbol.Symbol newName = Symbol.Symbol.symbol(params.name.toString() + "_" + count);
				count++;
				env.venv.put(params.name, newName);
				params.name = newName;
				Symbol.Symbol typ = (Symbol.Symbol)env.tenv.get(params.typ);
				if(typ != null)
					params.typ = typ;
				params = params.tail;
			}
			ReNaming rename = new ReNaming(dd);
			dd.body = rename.renameExp(dd.body);
			env.tenv.endScope();
			env.venv.endScope();
			dd = dd.next;
		}
		return d;
	}
	
	public TypeDec renameDec(TypeDec d){
		TypeDec dd = d;
		while(dd != null){
			Symbol.Symbol newName = Symbol.Symbol.symbol(dd.name.toString() + "_" + count);
			count++;
			env.tenv.put(dd.name, newName);
			dd.name = newName;
			dd = dd.next;
		}
		dd = d;
		while(dd != null){
			dd.ty = renameTy(dd.ty);
			dd = dd.next;
		}
		return d;
	}
	public Ty renameTy (Ty t){
		if(t instanceof NameTy)return renameTy((NameTy)t);
		else if(t instanceof ArrayTy)return renameTy((ArrayTy)t);
		else if(t instanceof RecordTy)return renameTy((RecordTy)t);
		else throw new Error("renameTy");
	}
	public NameTy renameTy(NameTy t){
		Symbol.Symbol name = (Symbol.Symbol)env.tenv.get(t.name);
		if(name != null){
			t.name = name;
		}
		return t;
	}
	public ArrayTy renameTy(ArrayTy t){
		Symbol.Symbol typ = (Symbol.Symbol)env.tenv.get(t.typ);
		if(typ != null){
			t.typ = typ;
		}
		return t;
	}
	public RecordTy renameTy(RecordTy t){
		FieldList fields = t.fields;
		while (fields != null){
			Symbol.Symbol typ = (Symbol.Symbol)env.tenv.get(fields.typ);
			if (typ != null){
				fields.typ = typ;
			}
			fields = fields.tail;
		}
		return t;
	}
}