package findescape;

abstract class Escape{
	int depth;
	abstract void setEscape();
}

class FormalEscape extends Escape{
	absyn.FieldList fl;
	FormalEscape(int d, absyn.FieldList f){
		depth = d;
		fl = f;
		fl.escape = false;
	}
	void setEscape(){fl.escape = true;}
}

class VarEscape extends Escape{
	absyn.VarDec vd;
	VarEscape(int d, absyn.VarDec v){
		depth = d;
		vd = v;
		vd.escape = false;
	}
	void setEscape(){vd.escape = true;}
}

public class FindEscape{
	symbol.Table escEnv = new symbol.Table();
	void traverseVar(int depth, absyn.Var v){
		if(v instanceof absyn.SimpleVar) traverseVar(depth, (absyn.SimpleVar)v);
		else if (v instanceof absyn.FieldVar) traverseVar(depth, (absyn.FieldVar)v);
		else if(v instanceof absyn.SubscriptVar) traverseVar(depth, (absyn.SubscriptVar)v);
	}
	void traverseVar(int depth, absyn.SimpleVar v){
		Escape es = (Escape)escEnv.get(v.name);
		if(es == null)return;
		if(es.depth < depth)es.setEscape();
	}
	void traverseVar(int depth, absyn.FieldVar v){
		traverseVar(depth, v.var);
	}
	void traverseVar(int depth, absyn.SubscriptVar v){
		traverseVar(depth, v.var);
		traverseExp(depth, v.index);
	}

	public int traverseExp(int depth, absyn.Exp e){
		if(e instanceof absyn.VarExp) return traverseExp(depth, (absyn.VarExp)e);
		else if(e instanceof absyn.NilExp) return traverseExp(depth, (absyn.NilExp)e);
		else if(e instanceof absyn.IntExp) return traverseExp(depth, (absyn.IntExp)e);
		else if(e instanceof absyn.StringExp) return traverseExp(depth, (absyn.StringExp)e);
		else if(e instanceof absyn.CallExp) return traverseExp(depth, (absyn.CallExp)e);
		else if(e instanceof absyn.OpExp) return traverseExp(depth, (absyn.OpExp)e);
		else if(e instanceof absyn.RecordExp) return traverseExp(depth, (absyn.RecordExp)e);
		else if(e instanceof absyn.SeqExp) return traverseExp(depth, (absyn.SeqExp)e);
		else if(e instanceof absyn.AssignExp) return traverseExp(depth, (absyn.AssignExp)e);
		else if(e instanceof absyn.IfExp) return traverseExp(depth, (absyn.IfExp)e);
		else if(e instanceof absyn.WhileExp) return traverseExp(depth, (absyn.WhileExp)e);
		else if(e instanceof absyn.ForExp) return traverseExp(depth, (absyn.ForExp)e);
		else if(e instanceof absyn.BreakExp) return traverseExp(depth, (absyn.BreakExp)e);
		else if(e instanceof absyn.LetExp) return traverseExp(depth, (absyn.LetExp)e);
		else if(e instanceof absyn.ArrayExp) return traverseExp(depth, (absyn.ArrayExp)e);
		else throw new Error("traverseExp");
	}
	int traverseExp(int depth, absyn.VarExp e){
		traverseVar(depth, e.var);
		return 0;
	}
	int traverseExp(int depth, absyn.NilExp e){
		return 0;
	}
	int traverseExp(int depth, absyn.IntExp e){
		return 0;
	}
	int traverseExp(int depth, absyn.StringExp e){
		return 0;
	}
	int traverseExp(int depth, absyn.CallExp e){
		absyn.ExpList args = e.args;
		int size = 0;
		while(args != null){
			args = args.tail;
			size ++;
		}
		traverseExpList(depth, e.args);
		return size;
	}
	int traverseExp(int depth, absyn.OpExp e){
		int x = traverseExp(depth, e.left);
		int y = traverseExp(depth, e.right);
		return (x>y?x:y);
	}
	int traverseExp(int depth, absyn.RecordExp e){
		absyn.FieldExpList fel = e.fields;
		int size = 0;
		while(fel != null){
			int t = traverseExp(depth, fel.init);
			fel = fel.tail;
			if(t > size)size = t;
		}
		return size;
	}
	int traverseExp(int depth, absyn.SeqExp e){
		return traverseExpList(depth, e.list);
	}
	int traverseExpList(int depth, absyn.ExpList e){
		int size = 0;
		while(e != null){
			int t = traverseExp(depth, e.head);
			e = e.tail;
			if(t > size)size = t;
		}
		return size;
	}
	int traverseExp(int depth, absyn.AssignExp e){
		traverseVar(depth, e.var);
		return traverseExp(depth, e.exp);
	}
	int traverseExp(int depth, absyn.IfExp e){
		int t;
		int size;
		size = traverseExp(depth, e.test);
		t = traverseExp(depth, e.thenclause);
		if(t > size)size = t;
		if(e.elseclause != null)t = traverseExp(depth, e.elseclause);
		if(t > size)size = t;
		return size;
	}
	int traverseExp(int depth, absyn.WhileExp e){
		int x = traverseExp(depth, e.test);
		int y = traverseExp(depth, e.body);
		return (x>y?x:y);
	}
	int traverseExp(int depth, absyn.ForExp e){
		int x = traverseExp(depth, e.var.init);
		int y = traverseExp(depth, e.hi);
		escEnv.beginScope();
		escEnv.put(e.var.name, new VarEscape(depth, e.var));
		int z = traverseExp(depth, e.body);
		escEnv.endScope();
		if(x >= y && x >=z)return x;
		if(y >= x && y >=z)return y;
		return z;
	}
	int traverseExp(int depth, absyn.BreakExp e){
		return 0;
	}
	int traverseExp(int depth, absyn.LetExp e){
		absyn.DecList dl = e.decs;
		int size = 0;
		escEnv.beginScope();
		while(dl != null){
			int t = traverseDec(depth, dl.head);
			if(t > size)size = t;
			dl = dl.tail;
		}
		int t = traverseExp(depth, e.body);
		escEnv.endScope();
		if(t > size)size = t;
		return size;
	}
	int traverseExp(int depth, absyn.ArrayExp e){
		int x = traverseExp(depth, e.size);
		int y = traverseExp(depth, e.init);
		return (x>y?x:y);
	}

	int traverseDec(int depth, absyn.Dec d){
		if(d instanceof absyn.VarDec)return  traverseDec(depth, (absyn.VarDec)d);
		else if(d instanceof absyn.FunctionDec)return traverseDec(depth, (absyn.FunctionDec)d);
		else if(d instanceof absyn.TypeDec)return traverseDec(depth, (absyn.TypeDec)d);
		else throw new Error("traverseDec");
	}
	int traverseDec(int depth, absyn.VarDec d){
		int size = traverseExp(depth, d.init);
		escEnv.put(d.name, new VarEscape(depth, d));
		return size;
	}
	int traverseDec(int depth, absyn.TypeDec d){
		return 0;
	}
	int traverseDec(int depth, absyn.FunctionDec d){
		absyn.FieldList fl = d.params;
		escEnv.beginScope();
		while(fl != null){
			escEnv.put(fl.name, new FormalEscape(depth + 1, fl));
			fl = fl.tail;
		}
		d.outGoing = traverseExp(depth + 1, d.body);
		escEnv.endScope();
		if(d.next != null)traverseDec(depth, d.next);
		return 0;
	}
	public FindEscape(){}
}