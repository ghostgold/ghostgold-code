package Semant;
import Translate.Exp;
public class Semant
{
	Env env;
	public static final Types.INT INT = new Types.INT();
	public static final Types.NIL NIL = new Types.NIL();
	public static final Types.STRING STRING = new Types.STRING();
	public static final Types.VOID VOID = new Types.VOID();
	public static final Types.RECORD UNKNOWN = new Types.RECORD(null, null, null);
	public boolean semantError;
	public Symbol.Symbol BREAK;
	public Semant(ErrorMsg.ErrorMsg err){
		this(new Env(err));
		semantError = false;
		BREAK = Symbol.Symbol.symbol("break");
		env.tenv.put(BREAK, INT);
	}
	public Semant(Env e){env = e;}
	
	public void transProg(Absyn.Exp exp){
		transExp(exp);
	}

	void error(int pos, String msg){
		env.errorMsg.error(pos,msg);
		semantError = true;
	}

	ExpTy transVar(Absyn.Var e){
		if(e instanceof Absyn.SimpleVar)return transVar((Absyn.SimpleVar)e);
		else if (e instanceof Absyn.FieldVar)return transVar((Absyn.FieldVar)e);
		else if(e instanceof Absyn.SubscriptVar)return transVar((Absyn.SubscriptVar)e);
		throw new Error("transVar");
	}
	ExpTy transVar(Absyn.SimpleVar e){
		Entry x = (Entry)env.venv.get(e.name);
		if(x instanceof VarEntry){
			VarEntry ent = (VarEntry)x;
			return new ExpTy(null, ent.ty);
		}
		else {
			error(e.pos, "undefined variable");
			return new ExpTy(null, UNKNOWN);
		}
	}
	ExpTy transVar(Absyn.FieldVar e){
		ExpTy var = transVar(e.var);
		if(var.ty.actual() instanceof Types.RECORD){
			Types.RECORD type = (Types.RECORD)var.ty;
			Types.Type f = type.check(e.field);
			if(f != null)
				return new ExpTy(null,f);
			else {
				error(e.pos, "The record do not have a field with name" + e.field.toString());
				return new ExpTy(null,UNKNOWN);
			}
		}
		else {
			error(e.pos, "This is not an object of a record type");
			return new ExpTy(null, UNKNOWN);
		}
	}
	ExpTy transVar(Absyn.SubscriptVar e){
		ExpTy var = transVar(e.var);
		if(var.ty.actual() instanceof Types.ARRAY){
			Types.ARRAY type = (Types.ARRAY)(var.ty.actual());
			ExpTy index = transExp(e.index);
			if(index.ty.actual() instanceof Types.INT){
				return new ExpTy(null, type.element);
			}
			else {
				error(e.pos, "The subscript should be an int");
				return new ExpTy(null, UNKNOWN);
			}
		}
		else {
			error(e.pos, "This is not array type");
			return new ExpTy(null, UNKNOWN);
		}
	}

	ExpTy transExp(Absyn.Exp e){
		if(e instanceof Absyn.VarExp)return transExp((Absyn.VarExp)e);
		else if(e instanceof Absyn.NilExp)return transExp((Absyn.NilExp)e);
		else if(e instanceof Absyn.IntExp)return transExp((Absyn.IntExp)e);
		else if(e instanceof Absyn.StringExp)return transExp((Absyn.StringExp)e);
		else if(e instanceof Absyn.CallExp)return transExp((Absyn.CallExp)e);
		else if(e instanceof Absyn.OpExp)return transExp((Absyn.OpExp)e);
		else if(e instanceof Absyn.RecordExp)return transExp((Absyn.RecordExp)e);
		else if(e instanceof Absyn.SeqExp)return transExp((Absyn.SeqExp)e);
		else if(e instanceof Absyn.AssignExp)return transExp((Absyn.AssignExp)e);
		else if(e instanceof Absyn.IfExp)return transExp((Absyn.IfExp)e);
		else if(e instanceof Absyn.WhileExp)return transExp((Absyn.WhileExp)e);
		else if(e instanceof Absyn.ForExp)return transExp((Absyn.ForExp)e);
		else if(e instanceof Absyn.BreakExp)return transExp((Absyn.BreakExp)e);
		else if(e instanceof Absyn.LetExp)return transExp((Absyn.LetExp)e);
		else if(e instanceof Absyn.ArrayExp)return transExp((Absyn.ArrayExp)e);
		else throw new Error("transExp");
	}
	ExpTy transExp(Absyn.VarExp e)	{
		return transVar(e.var);
	}
	ExpTy transExp(Absyn.NilExp e) {
		return new ExpTy(null, NIL);
	}
	ExpTy transExp(Absyn.IntExp e) {
		return new ExpTy(null , INT);
	}
	ExpTy transExp(Absyn.StringExp e){
		return new ExpTy(null , STRING);
	}
	ExpTy transExp(Absyn.CallExp e){
		Entry x = (Entry)env.venv.get(e.func);
		if(x instanceof FunEntry){
			FunEntry f = (FunEntry)x;
			Types.RECORD r = f.formals;
			Absyn.ExpList args = e.args;
			while(args != null && r != null){
				ExpTy arg = transExp(args.head);
				if(!arg.ty.coerceTo(r.fieldType))
					error(args.head.pos, "The type of argment is different from the declaration");
				args = args.tail;
				r = r.tail;
			}
			if(args == null && r == null)
				return new ExpTy(null, f.result);
			else if(args != null){
				error(e.pos, "Too many argments");
				return new ExpTy(null, f.result);
			}
			else if(r != null){
				error(e.pos, "THe argments is not enough");
				return new ExpTy(null, f.result);
			}
		}
		else 
			error(e.pos, "not a function");
		return new ExpTy(null, UNKNOWN);
	}
	ExpTy transExp(Absyn.OpExp e){
		ExpTy left = transExp(e.left);
		ExpTy right = transExp(e.right);
		switch(e.oper){
		case Absyn.OpExp.PLUS:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(!left.ty.coerceTo(INT)){
				error(e.left.pos, "integer required");
				return new ExpTy(null, INT);
			}
			else if(!right.ty.coerceTo(INT)){
				error(e.right.pos, "integer required");
			}
			break;
		case Absyn.OpExp.MINUS:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(!left.ty.coerceTo(INT)){
				error(e.left.pos, "integer required");
				return new ExpTy(null, INT);
			}
			else if(!right.ty.coerceTo(INT)){
				error(e.right.pos, "integer required");
			}
			break;
		case Absyn.OpExp.MUL:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(!left.ty.coerceTo(INT)){
				error(e.left.pos, "integer required");
				return new ExpTy(null, INT);
			}
			else if(!right.ty.coerceTo(INT)){
				error(e.right.pos, "integer required");
			}
			break;
		case Absyn.OpExp.DIV:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(!left.ty.coerceTo(INT)){
				error(e.left.pos, "integer required");
				return new ExpTy(null, INT);
			}
			else if(!right.ty.coerceTo(INT)){
				error(e.right.pos, "integer required");
			}
			break;
		case Absyn.OpExp.EQ:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(null, INT);
			else if((left.ty.coerceTo(right.ty) || right.ty.coerceTo(left.ty)) 
					&& !(left.ty.coerceTo(NIL) && right.ty.coerceTo(NIL)))
				return new ExpTy(null, INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING))){
				error(e.left.pos, "integer or string required");
				return new ExpTy(null, INT);
			}
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING))){
				error(e.right.pos, "integer required");
			}
			break;
		case Absyn.OpExp.NE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(right.ty) || right.ty.coerceTo(left.ty)
					&& !(left.ty.coerceTo(NIL) && right.ty.coerceTo(NIL)))
				return new ExpTy(null, INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING))){
				error(e.left.pos, "integer or string required");
				return new ExpTy(null, INT);
			}
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING))){
				error(e.right.pos, "integer required");
			}
			break;
		case Absyn.OpExp.LT:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(null, INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING))){
				error(e.left.pos, "integer or string required");
				return new ExpTy(null, INT);
			}
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING))){
				error(e.right.pos, "integer required");
			}
			break;
		case Absyn.OpExp.LE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(null, INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING))){
				error(e.left.pos, "integer or string required");
				return new ExpTy(null, INT);
			}
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING))){
				error(e.right.pos, "integer required");
			}
			break;
		case Absyn.OpExp.GT:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(null, INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING))){
				error(e.left.pos, "integer or string required");
				return new ExpTy(null, INT);
			}
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING))){
				error(e.right.pos, "integer required");
			}
			break;
		case Absyn.OpExp.GE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(null, INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING))){
				error(e.left.pos, "integer or string required");
				return new ExpTy(null, INT);
			}
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING))){
				error(e.right.pos, "integer required");
			}
			break;
		default:
			return new ExpTy(null, INT);
		}
		return new ExpTy(null, INT);
	}
	ExpTy transExp(Absyn.RecordExp e){
		Types.Type typ = (Types.Type)env.tenv.get(e.typ);
		if(typ instanceof Types.RECORD){
			Absyn.FieldExpList f = e.fields;
			Types.RECORD save = (Types.RECORD)typ;
			Types.RECORD r = save;
			while(f != null && r != null){
				if(f.name != r.fieldName){
					error(f.pos, "record " + e.typ.toString() + " has no field with name" 
						  + f.name.toString());
				}
				ExpTy init = transExp(f.init);
				if(!init.ty.coerceTo(r.fieldType)){
					error(f.pos, "type of exp is different with the type of " 
						  + r.fieldName.toString() + " in declaration");
				}
			}
			if(f != null)
				error(e.pos, "too many fields");
			else if(r != null)
				error(e.pos, "the fields is not enough");
			return new ExpTy(null, save);
		}
		else {
			error(e.pos, "undefined type or not a record type");
			return new ExpTy(null, UNKNOWN);
		}
	}
	ExpTy transExp(Absyn.SeqExp e){
		return transExpList(e.list);
	}
	ExpTy transExpList(Absyn.ExpList list){
		while(list != null){
			ExpTy t = transExp(list.head);
			if(list.tail == null)return t;
			list = list.tail;
		}
		return new ExpTy(null, VOID);
	}
	ExpTy transExp(Absyn.AssignExp e){
		ExpTy var = transVar(e.var);
		ExpTy exp = transExp(e.exp);
		if(exp.ty.coerceTo(var.ty))return new ExpTy(null, VOID);
		else {
			error(e.pos, "assignment can not be proceeded");
			return new ExpTy(null, VOID);
		}
	}
	ExpTy transExp(Absyn.IfExp e){
		ExpTy test = transExp(e.test);
		if(!test.ty.coerceTo(INT)){
			error(e.pos, "integer required");
		}
		ExpTy thenclause = transExp(e.thenclause);
		if(e.elseclause != null){
			ExpTy elseclause = transExp(e.elseclause);
			if(!(thenclause.ty.coerceTo(elseclause.ty) 
				 || elseclause.ty.coerceTo(thenclause.ty))){
				error(e.pos, "then and else should have the same type");
				return new ExpTy(null, UNKNOWN);
			}
			else {
				if(!thenclause.ty.coerceTo(NIL))return new ExpTy(null, thenclause.ty);
				else return new ExpTy(null, elseclause.ty);
			}
		}
		else {
			return new ExpTy(null, VOID);
		}
	}
	ExpTy transExp(Absyn.WhileExp e){
		ExpTy test = transExp(e.test);
		if(!test.ty.coerceTo(INT))
			error(e.pos, "integer required");
		env.tenv.beginScope();
		env.tenv.put(BREAK, STRING);
		ExpTy body = transExp(e.body);
		env.tenv.endScope();
		if(!body.ty.coerceTo(VOID))
			error(e.pos, "no value should be produced by the body of a while loop");
		return new ExpTy(null, VOID);
	}
	ExpTy transExp(Absyn.ForExp e){
		ExpTy hi = transExp(e.hi);
		if(!hi.ty.coerceTo(INT)){
			error(e.pos, "upper bound should be integer");
		}
		env.venv.beginScope();
		Exp var = transDec(e.var);
		Types.Type ty = ((VarEntry)(env.venv.get(e.var.name))).ty;
		if(!ty.coerceTo(INT)){
			error(e.pos, "lower bound should be integer");
		}
		env.tenv.beginScope();
		env.tenv.put(BREAK, STRING);
		ExpTy body = transExp(e.body);
		env.venv.endScope();
		env.tenv.endScope();
		if(!body.ty.coerceTo(VOID))
			error(e.pos, "no value should be produced by the body of a while loop");
		return new ExpTy(null, VOID);
	}
	ExpTy transExp(Absyn.BreakExp e){
		Types.Type b = (Types.Type)(env.venv.get(BREAK);
		if(b.coerceTo(INT))
			error(e.pos, "break must be in a loop");
		return new ExpTy(null, VOID);
	}
	ExpTy transExp(Absyn.LetExp e){
		env.venv.beginScope();
		env.tenv.beginScope();
		for(Absyn.DecList p = e.decs; p != null; p = p.tail)
			transDec(p.head);
		ExpTy body = transExpList(e.body);
		env.venv.endScope();
		env.tenv.endScope();
		return new ExpTy(null, body.ty);
	}
	ExpTy transExp(Absyn.ArrayExp e){
		Types.Type ty = (Types.Type)(env.tenv.get(e.typ));
		if(!(ty.actual() instanceof Types.ARRAY))
			error(e.pos, "type should be an array type");
		else {
			ExpTy size = transExp(e.size);
			if(!size.ty.coerceTo(INT))
				error(e.pos, "size should be an integer");
			ExpTy init = transExp(e.init);
			if(!init.ty.coerceTo(((Types.ARRAY)ty).element))
				error(e.pos, 
					  "the type of initial value is different from the type of array elements");
		}
		return new ExpTy(null, ty);
	}
	Exp transDec(Absyn.Dec e){
		if(e instanceof Absyn.VarDec)return transDec((Absyn.VarDec)e);
		else if(e instanceof Absyn.FunctionDec)return transDec((Absyn.FunctionDec)e);
		else if(e instanceof Absyn.TypeDec)return transDec((Absyn.TypeDec)e);
		else throw new Error("transDec");
	}
	Exp transDec(Absyn.VarDec e){
		ExpTy init = transExp(e.init);
		if(e.typ == null){
			if(init.ty.coerceTo(NIL))
				error(e.pos, "type must be given for nil");
			else env.venv.put(e.name, new VarEntry(transExp(e.init).ty));
		}
		else {
		
			Types.Type ty = transTy(e.typ);
			if(!init.ty.coerceTo(ty))
				error(e.pos, "type of initial value is different with the type in declaration");
		}
		return null;
	}
	Exp transDec(Absyn.TypeDec e){
		if(checkSame(e)){
			error(e.pos, "same type-id in a sequence");
			return null;
		}
		for(Absyn.TypeDec p = e; p != null; p = p.next){
			env.tenv.put(p.name, new Types.NAME(p.name));
		}
		for(Absyn.TypeDec p = e; p != null; p = p.next){
			Types.NAME name = (Types.NAME)(env.tenv.get(p.name));
			name.bind(transTy(p.ty));
		}
		for(Absyn.TypeDec p = e; p != null; p = p.next){
			Types.NAME name = (Types.NAME)(env.tenv.get(p.name));
			if(name.isLoop())
				error(p.pos, "type recursion cycle must pass through a record or array type");
		}
		return null;
	}
	Exp transDec(Absyn.FunctionDec e){
		if(checkSame(e)){
			error(e.pos, "same function-id in a sequence");
			return null;
		}
		for(Absyn.FunctionDec p = e; p != null; p = p.next){
			Types.Type result = transTy(e.result);
			Types.RECORD formals = transTypeField(e.params);
			env.venv.put(e.name, new FunEntry(formals, result));
		}
		for(Absyn.FunctionDec p = e; p != null; p = p.next){
			env.venv.beginScope();
			env.tenv.beginScope();
			env.tenv.put(BREAK, INT);
			for(Absyn.FieldList q = p.params; q != null; q = q.tail){
				env.venv.put(q.name, new VarEntry((Types.Type)(env.tenv.get(q.typ))));
			}
			transExp(p.body);
			env.venv.endScope();
			env.tenv.endScope();
		}
		return null;
	}
	Types.Type transTy(Absyn.Ty e){
		if(e instanceof Absyn.NameTy)return transTy((Absyn.NameTy)e);
		else if(e instanceof Absyn.RecordTy)return transTy((Absyn.RecordTy)e);
		else if(e instanceof Absyn.ArrayTy)return transTy((Absyn.ArrayTy)e);
		else throw new Error("transTy");
	}
	Types.Type transTy(Absyn.NameTy e){
		if(e.name == null)return VOID;
		Types.Type name = (Types.Type)(env.tenv.get(e.name));
		if(name == null){
			error(e.pos, "undefined type");
		}
		return name;
	}
	Types.RECORD transTy(Absyn.RecordTy e){
		return transTypeField(e.fields);
	}
	Types.ARRAY transTy(Absyn.ArrayTy e){
		Types.Type name = (Types.Type)(env.tenv.get(e.typ));
		if(name ==  null){
			error(e.pos, "undefined type");
		}
		return new Types.ARRAY(name);
	}
	Types.RECORD transTypeField(Absyn.FieldList e){
		if(checkSame(e)){
			error(e.pos,"same field name in a record");
		}
		Types.RECORD r = new Types.RECORD(e.name, (Types.Type)(env.tenv.get(e.typ)), null);
		Types.RECORD t = r;
		for(Absyn.FieldList p = e.tail; p != null; p = p.tail){
			t.tail = new Types.RECORD(p.name, (Types.Type)(env.tenv.get(p.typ)), null);
			t = t.tail;
		}
		return r;

	}
	boolean checkSame(Absyn.TypeDec e){
		for(Absyn.TypeDec p = e.next; p != null; p = p.next){
			if(p.name == e.name)return false;
		}
		if(e.next == null)return true;
		else return checkSame(e.next);
	}
	boolean checkSame(Absyn.FunctionDec e){
		for(Absyn.FunctionDec p = e.next; p != null; p = p.next){
			if(p.name == e.name)return false;
		}
		if(e.next == null)return true;
		else return checkSame(e.next);
	}
	boolean checkSame(Absyn.FieldList e){
		for(Absyn.FieldList p = e.tail; p != null; p = p.tail){
			if(p.name == e.name)return false;
		}
		if(e.tail == null)return true;
		else return checkSame(e.tail);
	}
}