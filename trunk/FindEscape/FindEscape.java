package FindEscape;

abstract class Escape{
	int depth;
	abstract void setEscape();
}

class FormalEscape extends Escape{
	Absyn.FieldList fl;
	FormalEscape(int d, Absyn.FieldList f){
		depth = d;
		fl = f;
		fl.escape = false;
	}
	void setEscape(){fl.escape = true;}
}

class VarEscape extends Escape{
	Absyn.VarDec vd;
	VarEscape(int d, Absyn.VarDec v){
		depth = d;
		vd = v;
		vd.escape = false;
	}
	void setEscape(){vd.escape = true;}
}

public class FindEscape{
	Symbol.Table escEnv = new Symbol.Table();
	void traverseVar(int depth, Absyn.Var v){
		if(v instanceof Absyn.SimpleVar) traverseVar(depth, (Absyn.SimpleVar)v);
		else if (v instanceof Absyn.FieldVar) traverseVar(depth, (Absyn.FieldVar)v);
		else if(v instanceof Absyn.SubscriptVar) traverseVar(depth, (Absyn.SubscriptVar)v);
	}
	void traverseVar(int depth, Absyn.SimpleVar v){
		Escape es = (Escape)escEnv.get(v.name);
		if(es == null)return;
		if(es.depth < depth)es.setEscape();
	}
	void traverseVar(int depth, Absyn.FieldVar v){
		traverseVar(depth, v.var);
	}
	void traverseVar(int depth, Absyn.SubscriptVar v){
		traverseVar(depth, v.var);
		traverseExp(depth, v.index);
	}

	public int traverseExp(int depth, Absyn.Exp e){
		if(e instanceof Absyn.VarExp) return traverseExp(depth, (Absyn.VarExp)e);
		else if(e instanceof Absyn.NilExp) return traverseExp(depth, (Absyn.NilExp)e);
		else if(e instanceof Absyn.IntExp) return traverseExp(depth, (Absyn.IntExp)e);
		else if(e instanceof Absyn.StringExp) return traverseExp(depth, (Absyn.StringExp)e);
		else if(e instanceof Absyn.CallExp) return traverseExp(depth, (Absyn.CallExp)e);
		else if(e instanceof Absyn.OpExp) return traverseExp(depth, (Absyn.OpExp)e);
		else if(e instanceof Absyn.RecordExp) return traverseExp(depth, (Absyn.RecordExp)e);
		else if(e instanceof Absyn.SeqExp) return traverseExp(depth, (Absyn.SeqExp)e);
		else if(e instanceof Absyn.AssignExp) return traverseExp(depth, (Absyn.AssignExp)e);
		else if(e instanceof Absyn.IfExp) return traverseExp(depth, (Absyn.IfExp)e);
		else if(e instanceof Absyn.WhileExp) return traverseExp(depth, (Absyn.WhileExp)e);
		else if(e instanceof Absyn.ForExp) return traverseExp(depth, (Absyn.ForExp)e);
		else if(e instanceof Absyn.BreakExp) return traverseExp(depth, (Absyn.BreakExp)e);
		else if(e instanceof Absyn.LetExp) return traverseExp(depth, (Absyn.LetExp)e);
		else if(e instanceof Absyn.ArrayExp) return traverseExp(depth, (Absyn.ArrayExp)e);
		else throw new Error("traverseExp");
	}
	int traverseExp(int depth, Absyn.VarExp e){
		traverseVar(depth, e.var);
		return 0;
	}
	int traverseExp(int depth, Absyn.NilExp e){
		return 0;
	}
	int traverseExp(int depth, Absyn.IntExp e){
		return 0;
	}
	int traverseExp(int depth, Absyn.StringExp e){
		return 0;
	}
	int traverseExp(int depth, Absyn.CallExp e){
		Absyn.ExpList args = e.args;
		int size = 0;
		while(args != null){
			args = args.tail;
			size ++;
		}
		traverseExpList(depth, e.args);
		return size;
	}
	int traverseExp(int depth, Absyn.OpExp e){
		int x = traverseExp(depth, e.left);
		int y = traverseExp(depth, e.right);
		return (x>y?x:y);
	}
	int traverseExp(int depth, Absyn.RecordExp e){
		Absyn.FieldExpList fel = e.fields;
		int size = 0;
		while(fel != null){
			int t = traverseExp(depth, fel.init);
			fel = fel.tail;
			if(t > size)size = t;
		}
		return size;
	}
	int traverseExp(int depth, Absyn.SeqExp e){
		return traverseExpList(depth, e.list);
	}
	int traverseExpList(int depth, Absyn.ExpList e){
		int size = 0;
		while(e != null){
			int t = traverseExp(depth, e.head);
			e = e.tail;
			if(t > size)size = t;
		}
		return size;
	}
	int traverseExp(int depth, Absyn.AssignExp e){
		traverseVar(depth, e.var);
		return traverseExp(depth, e.exp);
	}
	int traverseExp(int depth, Absyn.IfExp e){
		int t;
		int size;
		size = traverseExp(depth, e.test);
		t = traverseExp(depth, e.thenclause);
		if(t > size)size = t;
		if(e.elseclause != null)t = traverseExp(depth, e.elseclause);
		if(t > size)size = t;
		return size;
	}
	int traverseExp(int depth, Absyn.WhileExp e){
		int x = traverseExp(depth, e.test);
		int y = traverseExp(depth, e.body);
		return (x>y?x:y);
	}
	int traverseExp(int depth, Absyn.ForExp e){
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
	int traverseExp(int depth, Absyn.BreakExp e){
		return 0;
	}
	int traverseExp(int depth, Absyn.LetExp e){
		Absyn.DecList dl = e.decs;
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
	int traverseExp(int depth, Absyn.ArrayExp e){
		int x = traverseExp(depth, e.size);
		int y = traverseExp(depth, e.init);
		return (x>y?x:y);
	}

	int traverseDec(int depth, Absyn.Dec d){
		if(d instanceof Absyn.VarDec)return  traverseDec(depth, (Absyn.VarDec)d);
		else if(d instanceof Absyn.FunctionDec)return traverseDec(depth, (Absyn.FunctionDec)d);
		else if(d instanceof Absyn.TypeDec)return traverseDec(depth, (Absyn.TypeDec)d);
		else throw new Error("traverseDec");
	}
	int traverseDec(int depth, Absyn.VarDec d){
		int size = traverseExp(depth, d.init);
		escEnv.put(d.name, new VarEscape(depth, d));
		return size;
	}
	int traverseDec(int depth, Absyn.TypeDec d){
		return 0;
	}
	int traverseDec(int depth, Absyn.FunctionDec d){
		Absyn.FieldList fl = d.params;
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