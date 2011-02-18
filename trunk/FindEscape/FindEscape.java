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

	void traverseExp(int depth, Absyn.Exp e){
		if(e instanceof Absyn.VarExp) traverseExp(depth, (Absyn.VarExp)e);
		else if(e instanceof Absyn.NilExp) traverseExp(depth, (Absyn.NilExp)e);
		else if(e instanceof Absyn.IntExp) traverseExp(depth, (Absyn.IntExp)e);
		else if(e instanceof Absyn.StringExp) traverseExp(depth, (Absyn.StringExp)e);
		else if(e instanceof Absyn.CallExp) traverseExp(depth, (Absyn.CallExp)e);
		else if(e instanceof Absyn.OpExp) traverseExp(depth, (Absyn.OpExp)e);
		else if(e instanceof Absyn.RecordExp) traverseExp(depth, (Absyn.RecordExp)e);
		else if(e instanceof Absyn.SeqExp) traverseExp(depth, (Absyn.SeqExp)e);
		else if(e instanceof Absyn.AssignExp) traverseExp(depth, (Absyn.AssignExp)e);
		else if(e instanceof Absyn.IfExp) traverseExp(depth, (Absyn.IfExp)e);
		else if(e instanceof Absyn.WhileExp) traverseExp(depth, (Absyn.WhileExp)e);
		else if(e instanceof Absyn.ForExp) traverseExp(depth, (Absyn.ForExp)e);
		else if(e instanceof Absyn.BreakExp) traverseExp(depth, (Absyn.BreakExp)e);
		else if(e instanceof Absyn.LetExp) traverseExp(depth, (Absyn.LetExp)e);
		else if(e instanceof Absyn.ArrayExp) traverseExp(depth, (Absyn.ArrayExp)e);
	}
	void traverseExp(int depth, Absyn.VarExp e){
		traverseVar(depth, e.var);
	}
	void traverseExp(int depth, Absyn.NilExp e){
	}
	void traverseExp(int depth, Absyn.IntExp e){
	}
	void traverseExp(int depth, Absyn.StringExp e){
	}
	void traverseExp(int depth, Absyn.CallExp e){
		traverseExpList(depth, e.args);
	}
	void traverseExpList(int depth, Absyn.OpExp e){
		traverseExp(depth, e.left);
		traverseExp(depth, e.right);
	}
	void traverseExp(int depth, Absyn.RecordExp e){
		Absyn.FieldExpList fel = e.fields;
		while(fel != null){
			traverseExp(depth, fel.init);
			fel = fel.tail;
		}
	}
	void traverseExp(int depth, Absyn.SeqExp e){
		traverseExpList(depth, e.list);
	}
	void traverseExpList(int depth, Absyn.ExpList e){
		while(e != null){
			traverseExp(depth, e.head);
			e = e.tail;
		}
	}
	void traverseExp(int depth, Absyn.AssignExp e){
		traverseVar(depth, e.var);
		traverseExp(depth, e.exp);
	}
	void traverseExp(int depth, Absyn.IfExp e){
		traverseExp(depth, e.test);
		traverseExp(depth, e.thenclause);
		if(e.elseclause != null)traverseExp(depth, e.elseclause);
	}
	void traverseExp(int depth, Absyn.WhileExp e){
		traverseExp(depth, e.test);
		traverseExp(depth, e.body);
	}
	void traverseExp(int depth, Absyn.ForExp e){
		traverseDec(depth, e.var);
		traverseExp(depth, e.hi);
		traverseExp(depth, e.body);
	}
	void traverseExp(int depth, Absyn.BreakExp e){
	}
	void traverseExp(int depth, Absyn.LetExp e){
		Absyn.DecList dl = e.decs;
		while(dl != null){
			traverseDec(depth, dl.head);
			dl = dl.tail;
		}
		traverseExp(depth, e.body);
	}
	void traverseExp(int depth, Absyn.ArrayExp e){
		traverseExp(depth, e.size);
		traverseExp(depth, e.init);
	}

	void traverseDec(int depth, Absyn.Dec d){
		if(d instanceof Absyn.VarDec) traverseDec(depth, (Absyn.VarDec)d);
		else if(d instanceof Absyn.FunctionDec) traverseDec(depth, (Absyn.FunctionDec)d);
		else if(d instanceof Absyn.TypeDec) traverseDec(depth, (Absyn.TypeDec)d);
	}
	void traverseDec(int depth, Absyn.VarDec d){
		traverseExp(depth, d.init);
		escEnv.put(d.name, new VarEscape(depth, d));
	}
	void traverseDec(int depth, Absyn.TypeDec d){
	}
	void traverseDec(int depth, Absyn.FunctionDec d){
		Absyn.FieldList fl = d.params;
		while(fl != null){
			escEnv.put(fl.name, new FormalEscape(depth + 1, fl));
			fl = fl.tail;
		}
		traverseExp(depth + 1, d.body);
		traverseDec(depth, d.next);
	}
	public FindEscape(Absyn.Exp e){traverseExp(0, e);}
}