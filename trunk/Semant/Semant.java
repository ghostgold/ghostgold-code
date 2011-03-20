package Semant;
import Translate.Exp;
public class Semant
{
	Env env;
	public static final Types.INT INT = new Types.INT();
	public static final Types.INT INDEX = new Types.INT();
	public static final Types.NIL NIL = new Types.NIL();
	public static final Types.STRING STRING = new Types.STRING();
	public static final Types.VOID VOID = new Types.VOID();
	public static final Types.RECORD UNKNOWN = new Types.RECORD(null, null, null);
	public static boolean semantError;
	public Translate.Level level;
	public Translate.Translate translate;
	public void init(){
		env.tenv.put(Symbol.Symbol.symbol("int"),INT);
		env.tenv.put(Symbol.Symbol.symbol("string"),STRING);
		env.venv.put(Symbol.Symbol.symbol("print"), 
					 new FunEntry(level, new Temp.Label("print"),
								  new Types.RECORD(Symbol.Symbol.symbol("s"), STRING, null), VOID));
		env.venv.put(Symbol.Symbol.symbol("printi"), 
					 new FunEntry(level, new Temp.Label("printi"),
								  new Types.RECORD(Symbol.Symbol.symbol("i"), INT , null), VOID));
		env.venv.put(Symbol.Symbol.symbol("flush"),
					 new FunEntry(level, new Temp.Label("flush"),null , VOID));
		env.venv.put(Symbol.Symbol.symbol("getchar"), 
					 new FunEntry(level, new Temp.Label("getchar"),null, STRING));
		env.venv.put(Symbol.Symbol.symbol("ord"), 
					 new FunEntry(level, new Temp.Label("ord"),new Types.RECORD(Symbol.Symbol.symbol("s"), STRING, null), INT));
		env.venv.put(Symbol.Symbol.symbol("chr"), 
					 new FunEntry(level, new Temp.Label("chr"),new Types.RECORD(Symbol.Symbol.symbol("i"), INT, null), STRING));
		env.venv.put(Symbol.Symbol.symbol("size"), 
					 new FunEntry(level, new Temp.Label("size"),new Types.RECORD(Symbol.Symbol.symbol("s"), STRING, null), INT));
		env.venv.put(Symbol.Symbol.symbol("substring"), 
					 new FunEntry(level, new Temp.Label("substring"),new Types.RECORD(Symbol.Symbol.symbol("s"), STRING, 
								   new Types.RECORD(Symbol.Symbol.symbol("first"), INT, 
									new Types.RECORD(Symbol.Symbol.symbol("n"), INT, null))),
								  STRING));
		env.venv.put(Symbol.Symbol.symbol("concat"), 
					 new FunEntry(level, new Temp.Label("concat"), new Types.RECORD(Symbol.Symbol.symbol("s1"), STRING, 
								   new Types.RECORD(Symbol.Symbol.symbol("s2"), STRING, 
									null )),
								  STRING));
		env.venv.put(Symbol.Symbol.symbol("not"),
					 new FunEntry(level, new Temp.Label("not"), new Types.RECORD(Symbol.Symbol.symbol("i"), INT, null), INT));
		env.venv.put(Symbol.Symbol.symbol("exit"),
					 new FunEntry(level, new Temp.Label("exit"), new Types.RECORD(Symbol.Symbol.symbol("i"), INT, null), VOID));
		
	}
	public Semant(ErrorMsg.ErrorMsg err, Translate.Level lev){
		this(new Env(err), new Translate.Translate(), lev);
		semantError = false;
	}

	public Semant(Env e, Translate.Translate trans, Translate.Level lev){
		env = e;
		translate = trans;
		level = lev;
	}
	
	void error(int pos, String msg){
		env.errorMsg.error(pos,msg);
		semantError = true;
	}

	ExpTy transVar(Absyn.Var e, Temp.Label breakLabel){
		if(e instanceof Absyn.SimpleVar)return transVar((Absyn.SimpleVar)e, breakLabel);
		else if (e instanceof Absyn.FieldVar)return transVar((Absyn.FieldVar)e, breakLabel);
		else if(e instanceof Absyn.SubscriptVar)return transVar((Absyn.SubscriptVar)e, breakLabel);
		throw new Error("transVar");
	}
	ExpTy transVar(Absyn.SimpleVar e, Temp.Label breakLabel){
		Entry x = (Entry)env.venv.get(e.name);
		if(x instanceof VarEntry){
			VarEntry ent = (VarEntry)x;
			return new ExpTy(translate.simpleVar(ent.access, level), ent.ty);
		}
		else {
			error(e.pos, "undefined variable " + e.name);
			return new ExpTy(null, UNKNOWN);
		}
	}
	ExpTy transVar(Absyn.FieldVar e, Temp.Label breakLabel){
		ExpTy var = transVar(e.var, breakLabel);
		if(var.ty.actual() instanceof Types.RECORD){
			Types.RECORD type = (Types.RECORD)(var.ty.actual());
			Types.Type f = null;
			int index = 0;
			while(type != null){
				if(type.fieldName == e.field){
					f = type.fieldType;
					break;
				}
				type = type.tail;
				index++;
			}
			if(f != null)
				return new ExpTy(translate.fieldVar(var.exp, index, level),f);
			else {
				error(e.pos, "The record do not have a field with name " + e.field.toString());
				return new ExpTy(null,UNKNOWN);
			}
		}
		else {
			error(e.pos, "This is not an object of a record type");
			return new ExpTy(null, UNKNOWN);
		}
	}
	ExpTy transVar(Absyn.SubscriptVar e, Temp.Label breakLabel){
		ExpTy var = transVar(e.var, breakLabel);
		if(var.ty.actual() instanceof Types.ARRAY){
			Types.ARRAY type = (Types.ARRAY)(var.ty.actual());
			ExpTy index = transExp(e.index, breakLabel);
			if(index.ty.actual() instanceof Types.INT){
				return new ExpTy(translate.subscriptVar(var.exp, index.exp, level), type.element);
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

	ExpTy transExp(Absyn.Exp e, Temp.Label breakLabel){
		if(e instanceof Absyn.VarExp)return transExp((Absyn.VarExp)e, breakLabel);
		else if(e instanceof Absyn.NilExp)return transExp((Absyn.NilExp)e, breakLabel);
		else if(e instanceof Absyn.IntExp)return transExp((Absyn.IntExp)e, breakLabel);
		else if(e instanceof Absyn.StringExp)return transExp((Absyn.StringExp)e, breakLabel);
		else if(e instanceof Absyn.CallExp)return transExp((Absyn.CallExp)e, breakLabel);
		else if(e instanceof Absyn.OpExp)return transExp((Absyn.OpExp)e, breakLabel);
		else if(e instanceof Absyn.RecordExp)return transExp((Absyn.RecordExp)e, breakLabel);
		else if(e instanceof Absyn.SeqExp)return transExp((Absyn.SeqExp)e, breakLabel);
		else if(e instanceof Absyn.AssignExp)return transExp((Absyn.AssignExp)e, breakLabel);
		else if(e instanceof Absyn.IfExp)return transExp((Absyn.IfExp)e, breakLabel);
		else if(e instanceof Absyn.WhileExp)return transExp((Absyn.WhileExp)e, breakLabel);
		else if(e instanceof Absyn.ForExp)return transExp((Absyn.ForExp)e, breakLabel);
		else if(e instanceof Absyn.BreakExp)return transExp((Absyn.BreakExp)e, breakLabel);
		else if(e instanceof Absyn.LetExp)return transExp((Absyn.LetExp)e, breakLabel);
		else if(e instanceof Absyn.ArrayExp)return transExp((Absyn.ArrayExp)e, breakLabel);
		else throw new Error("transExp");
	}
	ExpTy transExp(Absyn.VarExp e, Temp.Label breakLabel)	{
		return transVar(e.var, breakLabel);
	}
	ExpTy transExp(Absyn.NilExp e, Temp.Label breakLabel) {
		return new ExpTy(translate.createNilExp(), NIL);
	}
	ExpTy transExp(Absyn.IntExp e, Temp.Label breakLabel) {
		return new ExpTy(translate.createIntExp(e.value) , INT);
	}
	ExpTy transExp(Absyn.StringExp e, Temp.Label breakLabel){
		return new ExpTy(translate.createStringExp(e.value) , STRING);
	}
	ExpTy transExp(Absyn.CallExp e, Temp.Label breakLabel){
		Entry x = (Entry)env.venv.get(e.func);
		if( x instanceof FunEntry){
			FunEntry f = (FunEntry)x;
			Types.RECORD r = f.formals;
			Absyn.ExpList args = e.args;
			Translate.ExpList argsexp = new Translate.ExpList(null, null);
			Translate.ExpList saveargs = argsexp;
			while(args != null && r != null){
				ExpTy arg = transExp(args.head, breakLabel);
				if(!arg.ty.coerceTo(r.fieldType))
					error(args.head.pos, "The type of actual argment is different from the formal ones");
				argsexp.tail = new Translate.ExpList(arg.exp, null);
				argsexp = argsexp.tail;
				args = args.tail;
				r = r.tail;
			}
			if(args == null && r == null)
				return new ExpTy(translate.createCallExp(f, saveargs.tail, level), f.result);
			else if(args != null){
				error(e.pos, "Too many actual argments");
				return new ExpTy(null, f.result);
			}
			else if(r != null){
				error(e.pos, "The actual argments is not enough");
				return new ExpTy(null, f.result);
			}
		}
		else 
			error(e.pos, "undefined function "+e.func.toString());
		return new ExpTy(null, UNKNOWN);
	}
	ExpTy transExp(Absyn.OpExp e, Temp.Label breakLabel){
		ExpTy left = transExp(e.left, breakLabel);
		ExpTy right = transExp(e.right, breakLabel);
		switch(e.oper){
		case Absyn.OpExp.PLUS:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createArithExp(Tree.BINOP.PLUS, left.exp, right.exp), INT);
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
				return new ExpTy(translate.createArithExp(Tree.BINOP.MINUS, left.exp,right.exp), INT);
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
				return new ExpTy(translate.createArithExp(Tree.BINOP.MUL, left.exp, right.exp), INT);
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
				return new ExpTy(translate.createArithExp(Tree.BINOP.DIV, left.exp, right.exp), INT);
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
				return new ExpTy(translate.createCompareExp(Tree.CJUMP.EQ, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(Tree.CJUMP.EQ, left.exp, right.exp, level), INT);
			else if((left.ty.coerceTo(right.ty) || right.ty.coerceTo(left.ty)) 
					&& !(left.ty.coerceTo(NIL) && right.ty.coerceTo(NIL)))
				return new ExpTy(translate.createCompareExp(Tree.CJUMP.EQ, left.exp, right.exp), INT);
			else error(e.pos, "compare between different type");
			//			System.out.println(left.ty.coerceTo(NIL));
			return new ExpTy(null, INT);

		case Absyn.OpExp.NE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(Tree.CJUMP.NE, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(Tree.CJUMP.NE, left.exp, right.exp, level), INT);
			else if(left.ty.coerceTo(right.ty) || right.ty.coerceTo(left.ty)
					&& !(left.ty.coerceTo(NIL) && right.ty.coerceTo(NIL)))
				return new ExpTy(translate.createCompareExp(Tree.CJUMP.NE, left.exp, right.exp), INT);
			else error(e.pos, "compare between different type");
			return new ExpTy(null, INT);

		case Absyn.OpExp.LT:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(Tree.CJUMP.LT, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(Tree.CJUMP.LT, left.exp, right.exp, level), INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING)))
				error(e.left.pos, "integer or string required");
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING)))
				error(e.right.pos, "integer or string required");
			else 
				error(e.pos, "compare between different type");
			return new ExpTy(null, INT);

		case Absyn.OpExp.LE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(Tree.CJUMP.LE, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(Tree.CJUMP.LE, left.exp, right.exp, level), INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING)))
				error(e.left.pos, "integer or string required");
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING)))
				error(e.right.pos, "integer or string required");
			else 
				error(e.pos, "compare between different type");
			return new ExpTy(null, INT);

		case Absyn.OpExp.GT:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(Tree.CJUMP.GT, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(Tree.CJUMP.GT, left.exp, right.exp, level), INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING)))
				error(e.left.pos, "integer or string required");
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING)))
				error(e.right.pos, "integer or string required");
			else 
				error(e.pos, "compare between different type");
			return new ExpTy(null, INT);

		case Absyn.OpExp.GE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(Tree.CJUMP.GE, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(Tree.CJUMP.GE, left.exp, right.exp, level), INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING)))
				error(e.left.pos, "integer or string required");
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING)))
				error(e.right.pos, "integer or string required");
			else 
				error(e.pos, "compare between different type");
			return new ExpTy(null, INT);

		default:
			return new ExpTy(null, INT);
		}
		return new ExpTy(null, INT);
	}


	ExpTy transExp(Absyn.RecordExp e, Temp.Label breakLabel){
		Types.Type typ = (Types.Type)(env.tenv.get(e.typ));
		if(typ != null && typ.actual() instanceof Types.RECORD){
			Absyn.FieldExpList f = e.fields;
			Types.RECORD save = (Types.RECORD)(typ.actual());
			Types.RECORD r = save;
			Translate.ExpList fieldexp = new Translate.ExpList(null, null);
			Translate.ExpList saveFieldExp = fieldexp;
			int fieldCount = 0;
			while(f != null && r != null){
				if(f.name != r.fieldName){
					error(f.pos, "record " + e.typ.toString() + " has no field with name " 
						  + f.name.toString());
					return new ExpTy(null, save);
				}
				ExpTy init = transExp(f.init, breakLabel);
				if(!init.ty.coerceTo(r.fieldType)){
					error(f.pos, "type of exp is different with the type of " 
						  + r.fieldName.toString() + " in declaration");
					return new ExpTy(null, save);
				}
				fieldexp.tail = new Translate.ExpList(init.exp, null);
				fieldexp = fieldexp.tail;
				f = f.tail;
				r = r.tail;
				fieldCount++;
			}
			if(f != null){
				error(e.pos, "too many fields");
				return new ExpTy(null, save);
			}
			else if(r != null){
				error(e.pos, "the fields is not enough");
				return new ExpTy(null, save);
			}
			else return new ExpTy(translate.createRecordExp(fieldCount, saveFieldExp.tail, level), save);
		}
		else {
			error(e.pos, "undefined type or not a record type " + e.typ.toString());
			return new ExpTy(null, UNKNOWN);
		}
	}
	ExpTy transExp(Absyn.SeqExp e, Temp.Label breakLabel){
		return transExpList(e.list, breakLabel);
		
	}
	ExpTy transExpList(Absyn.ExpList list, Temp.Label breakLabel){
		Translate.ExpList seq = new Translate.ExpList(null, null);
		Translate.ExpList saveseq = seq;
		ExpTy t = new ExpTy(null, VOID);
		while(list != null){
			t = transExp(list.head, breakLabel);
			seq.tail = new Translate.ExpList(t.exp, null);
			seq = seq.tail;
			if(list.tail == null)break;
			list = list.tail;
		}
		boolean stm = false;
		if(t.ty.coerceTo(VOID))stm = true;
		return new ExpTy(translate.createExpList(saveseq.tail, stm), t.ty);
	}
	ExpTy transExp(Absyn.AssignExp e, Temp.Label breakLabel){
		ExpTy var = transVar(e.var, breakLabel);
		ExpTy exp = transExp(e.exp, breakLabel);
		if(var.ty == INDEX){
			error(e.pos, "index varialbe can not be assign a value");
			return new ExpTy(null, VOID);
		}
		else if(exp.ty.coerceTo(var.ty))return new ExpTy(translate.createAssignExp(var.exp, exp.exp), VOID);
		else {
			error(e.pos, "assignment to variable a exp with different type");
			return new ExpTy(null, VOID);
		}
	}
	ExpTy transExp(Absyn.IfExp e, Temp.Label breakLabel){
		ExpTy test = transExp(e.test, breakLabel);
		if(!test.ty.coerceTo(INT)){
			error(e.pos, "integer required");
		}
		ExpTy thenclause = transExp(e.thenclause, breakLabel);
		if(e.elseclause != null){
			ExpTy elseclause = transExp(e.elseclause, breakLabel);
			if(!(thenclause.ty.coerceTo(elseclause.ty) 
				 || elseclause.ty.coerceTo(thenclause.ty))){
				error(e.pos, "then and else should have the same type");
				return new ExpTy(null, UNKNOWN);
			}
			else {
				if(!thenclause.ty.coerceTo(NIL))return new ExpTy(translate.createIfThenElse(test.exp, thenclause.exp, elseclause.exp), thenclause.ty);
				else return new ExpTy(translate.createIfThenElse(test.exp, thenclause.exp, elseclause.exp), elseclause.ty);
			}
		}
		else {
			if(!thenclause.ty.coerceTo(VOID))
				error(e.pos, "if-then should return no value");
			return new ExpTy(translate.createIfThenElse(test.exp, thenclause.exp, null), VOID);
		}
	}
	ExpTy transExp(Absyn.WhileExp e, Temp.Label breakLabel){
		ExpTy test = transExp(e.test, breakLabel);
		if(!test.ty.coerceTo(INT))
			error(e.pos, "integer required");
		Temp.Label finish = new Temp.Label();
		ExpTy body = transExp(e.body, finish);
		if(!body.ty.coerceTo(VOID))
			error(e.pos, "no value should be produced by the body of a while loop");
		return new ExpTy(translate.createWhileExp(test.exp, body.exp, finish), VOID);
	}
	ExpTy transExp(Absyn.ForExp e, Temp.Label breakLabel){
		ExpTy hi = transExp(e.hi, breakLabel);
		if(!hi.ty.coerceTo(INT)){
			error(e.pos, "upper bound should be integer");
		}
		ExpTy init = transExp(e.var.init, breakLabel);
		if(!init.ty.coerceTo(INT)){
			error(e.pos, "lower bound should be integer");
		}
		VarEntry loopVar = new VarEntry(level.allocLocal(e.var.escape), INDEX);
		Temp.Label finish = new Temp.Label();

		env.venv.beginScope();
		env.venv.put(e.var.name, loopVar);
		ExpTy body = transExp(e.body, finish);
		env.venv.endScope();
		if(!body.ty.coerceTo(VOID))
			error(e.pos, "no value should be produced by the body of a while loop");
		return new ExpTy(translate.createForExp(loopVar.access, init.exp, hi.exp, body.exp, level, finish), VOID);
	}
	ExpTy transExp(Absyn.BreakExp e, Temp.Label breakLabel){
		if(breakLabel == null)
			error(e.pos, "break must be in a loop");
		return new ExpTy(translate.createBreakExp(breakLabel), VOID);
	}
	ExpTy transExp(Absyn.LetExp e, Temp.Label breakLabel){
		env.venv.beginScope();
		env.tenv.beginScope();
		Translate.ExpList dec = new Translate.ExpList(null, null);
		Translate.ExpList savedec = dec;
		for(Absyn.DecList p = e.decs; p != null; p = p.tail){
			Translate.Exp t = transDec(p.head, breakLabel);
			if(t != null){
				dec.tail = new Translate.ExpList(t, null);
				dec = dec.tail;
			}
		}
		ExpTy body = transExp(e.body, breakLabel);
		env.venv.endScope();
		env.tenv.endScope();
		boolean ex = true;
		if(body.ty.coerceTo(VOID))ex = false;
		return new ExpTy(translate.createLetExp(savedec.tail, body.exp, ex), body.ty);
	}
	ExpTy transExp(Absyn.ArrayExp e, Temp.Label breakLabel){
		Types.Type ty = (Types.Type)(env.tenv.get(e.typ));
		if(ty != null && ty.actual() instanceof Types.ARRAY){
			ExpTy size = transExp(e.size, breakLabel);
			if(!size.ty.coerceTo(INT)){
				error(e.pos, "size should be an integer");
				return new ExpTy(null, ty);
			}
			ExpTy init = transExp(e.init, breakLabel);
			if(!init.ty.coerceTo(((Types.ARRAY)ty.actual()).element)){
				error(e.pos, 
					  "the type of initial value is different from the type of array elements");
				return new ExpTy(null, ty);
			}
			return new ExpTy(translate.createArrayExp(size.exp, init.exp, 
													  e.init, level), ty);
		}
		else{
			error(e.pos, "type should be an array type");
			return new ExpTy(null, UNKNOWN);
		}
	}
	Exp transDec(Absyn.Dec e, Temp.Label breakLabel){
		if(e instanceof Absyn.VarDec)return transDec((Absyn.VarDec)e, breakLabel);
		else if(e instanceof Absyn.FunctionDec)return transDec((Absyn.FunctionDec)e, breakLabel);
		else if(e instanceof Absyn.TypeDec)return transDec((Absyn.TypeDec)e, breakLabel);
		else throw new Error("transDec");
	}
	Exp transDec(Absyn.VarDec e, Temp.Label breakLabel){
		ExpTy init = transExp(e.init, breakLabel);
		if(e.typ == null){
			if(init.ty.coerceTo(NIL)){
				error(e.pos, "type must be given for nil");
				return translate.createNilExp();
			}
			else {
				VarEntry var = new VarEntry(level.allocLocal(e.escape), init.ty);
				env.venv.put(e.name, var);
				return translate.createVarDec(var.access, init.exp, level);
			}
		}
		else {
			Types.Type ty = transTy(e.typ);
			if(!init.ty.coerceTo(ty)){
				error(e.pos, "type of initial value is different with the type in declaration");
				return translate.createNilExp();
			}
			else {
				VarEntry var = new VarEntry(level.allocLocal(e.escape), ty);
				env.venv.put(e.name, var);
				return translate.createVarDec(var.access, init.exp, level);
			}
		}

	}
	Exp transDec(Absyn.TypeDec e, Temp.Label breakLabel){
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
	Exp transDec(Absyn.FunctionDec e, Temp.Label breakLabel){
		if(checkSame(e)){
			error(e.pos, "same function-id in a sequence");
			return null;
		}
		for(Absyn.FunctionDec p = e; p != null; p = p.next){
			Types.Type result = transTy(p.result);
			Types.RECORD formals = transTypeField(p.params);
			Temp.Label label = new Temp.Label();
			Util.BoolList escape = null;
			if(p.params != null)escape = p.params.getEscape();
			Translate.Level newLevel = new Translate.Level(level, label, escape);
			env.venv.put(p.name, new FunEntry(newLevel, label, formals, result));
		}

		for(Absyn.FunctionDec p = e; p != null; p = p.next){
			env.venv.beginScope();
			env.tenv.beginScope();
			FunEntry function = (FunEntry)env.venv.get(p.name);
			Translate.AccessList formalAccess = function.level.getFormals();
			for(Absyn.FieldList q = p.params; q != null; q = q.tail){
				Types.Type ty = (Types.Type)(env.tenv.get(q.typ));
				if(ty != null){
					env.venv.put(q.name, new VarEntry(formalAccess.head, ty));
					formalAccess = formalAccess.tail;
				}
				else 
					error(q.pos, "undefined type");
			}
			Semant newSemant = new Semant(env, translate, function.level);
			ExpTy body = newSemant.transExp(p.body, null);
			Types.Type result = transTy(p.result);
			if(!body.ty.coerceTo(result))
				error(p.pos, "return type of " + p.name + " different from declaration");
			if(result.coerceTo(VOID))translate.procEntryExit(function.level, body.exp, true);
			else translate.procEntryExit(function.level, body.exp, false);
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
		if(e == null)return VOID;
		Types.Type name = (Types.Type)(env.tenv.get(e.name));
		if(name == null){
			error(e.pos, "undefined type");
		}
		return name;
	}
	Types.RECORD transTy(Absyn.RecordTy e){
		Types.RECORD t = transTypeField(e.fields);
		if(t == null)
			return new Types.RECORD(null, null, null);
		else return t;

	}
	Types.ARRAY transTy(Absyn.ArrayTy e){
		Types.Type name = (Types.Type)(env.tenv.get(e.typ));
		if(name ==  null){
			error(e.pos, "undefined type");
		}
		return new Types.ARRAY(name);
	}
	Types.RECORD transTypeField(Absyn.FieldList e){
		if(e == null)return null;
		if(checkSame(e)){
			error(e.pos,"same field name in a record");
		}
		Types.Type typ = (Types.Type)(env.tenv.get(e.typ));
		if(typ == null)
			error(e.pos, "undefined type " + e.typ.toString());
		Types.RECORD r = new Types.RECORD(e.name, typ, null);
		Types.RECORD t = r;
		for(Absyn.FieldList p = e.tail; p != null; p = p.tail){
			Types.Type ty = (Types.Type)(env.tenv.get(p.typ));
			if(ty == null)error(p.pos, "undefined type " + p.typ.toString());
			t.tail = new Types.RECORD(p.name, ty, null);
			t = t.tail;
		}
		return r;

	}
	boolean checkSame(Absyn.TypeDec e){
		for(Absyn.TypeDec p = e.next; p != null; p = p.next){
			if(p.name == e.name)return true;
		}
		if(e.next == null)return false;
		else return checkSame(e.next);
	}
	boolean checkSame(Absyn.FunctionDec e){
		for(Absyn.FunctionDec p = e.next; p != null; p = p.next){
			if(p.name == e.name)return true;
		}
		if(e.next == null)return false;
		else return checkSame(e.next);
	}
	boolean checkSame(Absyn.FieldList e){
		for(Absyn.FieldList p = e.tail; p != null; p = p.tail){
			if(p.name == e.name)return true;
		}
		if(e.tail == null)return false;
		else return checkSame(e.tail);
	}
}