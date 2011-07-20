package semant;
import translate.Exp;
public class Semant
{
	Env env;
	public static final types.INT INT = new types.INT();
	public static final types.INT INDEX = new types.INT();
	public static final types.NIL NIL = new types.NIL();
	public static final types.STRING STRING = new types.STRING();
	public static final types.VOID VOID = new types.VOID();
	public static final types.RECORD UNKNOWN = new types.RECORD(null, null, null);
	public static boolean semantError;
	public translate.Level level;
	public translate.Translate translate;
	public void init(){
		env.tenv.put(symbol.Symbol.symbol("int"),INT);
		env.tenv.put(symbol.Symbol.symbol("string"),STRING);
		env.venv.put(symbol.Symbol.symbol("print"), 
					 new FunEntry(level, new temp.AtomicLabel("print", false),
								  new types.RECORD(symbol.Symbol.symbol("s"), STRING, null), VOID));
		env.venv.put(symbol.Symbol.symbol("printi"), 
					 new FunEntry(level, new temp.AtomicLabel("printi", false),
								  new types.RECORD(symbol.Symbol.symbol("i"), INT , null), VOID));
		env.venv.put(symbol.Symbol.symbol("flush"),
					 new FunEntry(level, new temp.AtomicLabel("flush", false),null , VOID));
		env.venv.put(symbol.Symbol.symbol("getchar"), 
					 new FunEntry(level, new temp.AtomicLabel("getchar", false),null, STRING));
		env.venv.put(symbol.Symbol.symbol("ord"), 
					 new FunEntry(level, new temp.AtomicLabel("ord", false),new types.RECORD(symbol.Symbol.symbol("s"), STRING, null), INT));
		env.venv.put(symbol.Symbol.symbol("chr"), 
					 new FunEntry(level, new temp.AtomicLabel("chr", false),new types.RECORD(symbol.Symbol.symbol("i"), INT, null), STRING));
		env.venv.put(symbol.Symbol.symbol("size"), 
					 new FunEntry(level, new temp.AtomicLabel("size", false),new types.RECORD(symbol.Symbol.symbol("s"), STRING, null), INT));
		env.venv.put(symbol.Symbol.symbol("substring"), 
					 new FunEntry(level, new temp.AtomicLabel("substring",false),new types.RECORD(symbol.Symbol.symbol("s"), STRING, 
								   new types.RECORD(symbol.Symbol.symbol("first"), INT, 
									new types.RECORD(symbol.Symbol.symbol("n"), INT, null))),
								  STRING));
		env.venv.put(symbol.Symbol.symbol("concat"), 
					 new FunEntry(level, new temp.AtomicLabel("concat", false), new types.RECORD(symbol.Symbol.symbol("s1"), STRING, 
								   new types.RECORD(symbol.Symbol.symbol("s2"), STRING, 
									null )),
								  STRING));
		env.venv.put(symbol.Symbol.symbol("not"),
					 new FunEntry(level, new temp.AtomicLabel("not", false), new types.RECORD(symbol.Symbol.symbol("i"), INT, null), INT));
		env.venv.put(symbol.Symbol.symbol("exit"),
					 new FunEntry(level, new temp.AtomicLabel("exit", false), new types.RECORD(symbol.Symbol.symbol("i"), INT, null), VOID));
		
	}
	public Semant(errormsg.ErrorMsg err, translate.Level lev){
		this(new Env(err), new translate.Translate(), lev);
		semantError = false;
	}

	public Semant(Env e, translate.Translate trans, translate.Level lev){
		env = e;
		translate = trans;
		level = lev;
	}
	
	void error(int pos, String msg){
		env.errorMsg.error(pos,msg);
		semantError = true;
	}

	ExpTy transVar(absyn.Var e, temp.AtomicLabel breakLabel){
		if(e instanceof absyn.SimpleVar)return transVar((absyn.SimpleVar)e, breakLabel);
		else if (e instanceof absyn.FieldVar)return transVar((absyn.FieldVar)e, breakLabel);
		else if(e instanceof absyn.SubscriptVar)return transVar((absyn.SubscriptVar)e, breakLabel);
		throw new Error("transVar");
	}
	ExpTy transVar(absyn.SimpleVar e, temp.AtomicLabel breakLabel){
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
	ExpTy transVar(absyn.FieldVar e, temp.AtomicLabel breakLabel){
		ExpTy var = transVar(e.var, breakLabel);
		if(var.ty.actual() instanceof types.RECORD){
			types.RECORD type = (types.RECORD)(var.ty.actual());
			types.Type f = null;
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
	ExpTy transVar(absyn.SubscriptVar e, temp.AtomicLabel breakLabel){
		ExpTy var = transVar(e.var, breakLabel);
		if(var.ty.actual() instanceof types.ARRAY){
			types.ARRAY type = (types.ARRAY)(var.ty.actual());
			ExpTy index = transExp(e.index, breakLabel);
			if(index.ty.actual() instanceof types.INT){
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

	public ExpTy transExp(absyn.Exp e, temp.AtomicLabel breakLabel){
		if(e instanceof absyn.VarExp)return transExp((absyn.VarExp)e, breakLabel);
		else if(e instanceof absyn.NilExp)return transExp((absyn.NilExp)e, breakLabel);
		else if(e instanceof absyn.IntExp)return transExp((absyn.IntExp)e, breakLabel);
		else if(e instanceof absyn.StringExp)return transExp((absyn.StringExp)e, breakLabel);
		else if(e instanceof absyn.CallExp)return transExp((absyn.CallExp)e, breakLabel);
		else if(e instanceof absyn.OpExp)return transExp((absyn.OpExp)e, breakLabel);
		else if(e instanceof absyn.RecordExp)return transExp((absyn.RecordExp)e, breakLabel);
		else if(e instanceof absyn.SeqExp)return transExp((absyn.SeqExp)e, breakLabel);
		else if(e instanceof absyn.AssignExp)return transExp((absyn.AssignExp)e, breakLabel);
		else if(e instanceof absyn.IfExp)return transExp((absyn.IfExp)e, breakLabel);
		else if(e instanceof absyn.WhileExp)return transExp((absyn.WhileExp)e, breakLabel);
		else if(e instanceof absyn.ForExp)return transExp((absyn.ForExp)e, breakLabel);
		else if(e instanceof absyn.BreakExp)return transExp((absyn.BreakExp)e, breakLabel);
		else if(e instanceof absyn.LetExp)return transExp((absyn.LetExp)e, breakLabel);
		else if(e instanceof absyn.ArrayExp)return transExp((absyn.ArrayExp)e, breakLabel);
		else throw new Error("transExp");
	}
	ExpTy transExp(absyn.VarExp e, temp.AtomicLabel breakLabel)	{
		return transVar(e.var, breakLabel);
	}
	ExpTy transExp(absyn.NilExp e, temp.AtomicLabel breakLabel) {
		return new ExpTy(translate.createNilExp(), NIL);
	}
	ExpTy transExp(absyn.IntExp e, temp.AtomicLabel breakLabel) {
		return new ExpTy(translate.createIntExp(e.value) , INT);
	}
	ExpTy transExp(absyn.StringExp e, temp.AtomicLabel breakLabel){
		return new ExpTy(translate.createStringExp(e.value) , STRING);
	}
	ExpTy transExp(absyn.CallExp e, temp.AtomicLabel breakLabel){
		Entry x = (Entry)env.venv.get(e.func);
		if( x instanceof FunEntry){
			FunEntry f = (FunEntry)x;
			types.RECORD r = f.formals;
			absyn.ExpList args = e.args;
			translate.ExpList argsexp = new translate.ExpList(null, null);
			translate.ExpList saveargs = argsexp;
			while(args != null && r != null){
				ExpTy arg = transExp(args.head, breakLabel);
				if(!arg.ty.coerceTo(r.fieldType))
					error(args.head.pos, "The type of actual argment is different from the formal ones");
				argsexp.tail = new translate.ExpList(arg.exp, null);
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
	ExpTy transExp(absyn.OpExp e, temp.AtomicLabel breakLabel){
		ExpTy left = transExp(e.left, breakLabel);
		ExpTy right = transExp(e.right, breakLabel);
		switch(e.oper){
		case absyn.OpExp.PLUS:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createArithExp(tree.BinOp.PLUS, left.exp, right.exp), INT);
			else if(!left.ty.coerceTo(INT)){
				error(e.left.pos, "integer required");
				return new ExpTy(null, INT);
			}
			else if(!right.ty.coerceTo(INT)){
				error(e.right.pos, "integer required");
			}
			break;
		case absyn.OpExp.MINUS:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createArithExp(tree.BinOp.MINUS, left.exp,right.exp), INT);
			else if(!left.ty.coerceTo(INT)){
				error(e.left.pos, "integer required");
				return new ExpTy(null, INT);
			}
			else if(!right.ty.coerceTo(INT)){
				error(e.right.pos, "integer required");
			}
			break;
		case absyn.OpExp.MUL:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createArithExp(tree.BinOp.MUL, left.exp, right.exp), INT);
			else if(!left.ty.coerceTo(INT)){
				error(e.left.pos, "integer required");
				return new ExpTy(null, INT);
			}
			else if(!right.ty.coerceTo(INT)){
				error(e.right.pos, "integer required");
			}
			break;
		case absyn.OpExp.DIV:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createArithExp(tree.BinOp.DIV, left.exp, right.exp), INT);
			else if(!left.ty.coerceTo(INT)){
				error(e.left.pos, "integer required");
				return new ExpTy(null, INT);
			}
			else if(!right.ty.coerceTo(INT)){
				error(e.right.pos, "integer required");
			}
			break;
		case absyn.OpExp.EQ:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(tree.Cjump.EQ, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(tree.Cjump.EQ, left.exp, right.exp, level), INT);
			else if((left.ty.coerceTo(right.ty) || right.ty.coerceTo(left.ty)) 
					&& !(left.ty.coerceTo(NIL) && right.ty.coerceTo(NIL)))
				return new ExpTy(translate.createCompareExp(tree.Cjump.EQ, left.exp, right.exp), INT);
			else error(e.pos, "compare between different type");
			//			System.out.println(left.ty.coerceTo(NIL));
			return new ExpTy(null, INT);

		case absyn.OpExp.NE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(tree.Cjump.NE, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(tree.Cjump.NE, left.exp, right.exp, level), INT);
			else if(left.ty.coerceTo(right.ty) || right.ty.coerceTo(left.ty)
					&& !(left.ty.coerceTo(NIL) && right.ty.coerceTo(NIL)))
				return new ExpTy(translate.createCompareExp(tree.Cjump.NE, left.exp, right.exp), INT);
			else error(e.pos, "compare between different type");
			return new ExpTy(null, INT);

		case absyn.OpExp.LT:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(tree.Cjump.LT, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(tree.Cjump.LT, left.exp, right.exp, level), INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING)))
				error(e.left.pos, "integer or string required");
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING)))
				error(e.right.pos, "integer or string required");
			else 
				error(e.pos, "compare between different type");
			return new ExpTy(null, INT);

		case absyn.OpExp.LE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(tree.Cjump.LE, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(tree.Cjump.LE, left.exp, right.exp, level), INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING)))
				error(e.left.pos, "integer or string required");
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING)))
				error(e.right.pos, "integer or string required");
			else 
				error(e.pos, "compare between different type");
			return new ExpTy(null, INT);

		case absyn.OpExp.GT:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(tree.Cjump.GT, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(tree.Cjump.GT, left.exp, right.exp, level), INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING)))
				error(e.left.pos, "integer or string required");
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING)))
				error(e.right.pos, "integer or string required");
			else 
				error(e.pos, "compare between different type");
			return new ExpTy(null, INT);

		case absyn.OpExp.GE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(translate.createCompareExp(tree.Cjump.GE, left.exp, right.exp), INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(translate.createStringCompareExp(tree.Cjump.GE, left.exp, right.exp, level), INT);
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


	ExpTy transExp(absyn.RecordExp e, temp.AtomicLabel breakLabel){
		types.Type typ = (types.Type)(env.tenv.get(e.typ));
		if(typ != null && typ.actual() instanceof types.RECORD){
			absyn.FieldExpList f = e.fields;
			types.RECORD save = (types.RECORD)(typ.actual());
			types.RECORD r = save;
			translate.ExpList fieldexp = new translate.ExpList(null, null);
			translate.ExpList saveFieldExp = fieldexp;
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
				fieldexp.tail = new translate.ExpList(init.exp, null);
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
	ExpTy transExp(absyn.SeqExp e, temp.AtomicLabel breakLabel){
		return transExpList(e.list, breakLabel);
		
	}
	ExpTy transExpList(absyn.ExpList list, temp.AtomicLabel breakLabel){
		translate.ExpList seq = new translate.ExpList(null, null);
		translate.ExpList saveseq = seq;
		ExpTy t = new ExpTy(null, VOID);
		while(list != null){
			t = transExp(list.head, breakLabel);
			seq.tail = new translate.ExpList(t.exp, null);
			seq = seq.tail;
			if(list.tail == null)break;
			list = list.tail;
		}
		boolean stm = false;
		if(t.ty.coerceTo(VOID))stm = true;
		return new ExpTy(translate.createExpList(saveseq.tail, stm), t.ty);
	}
	ExpTy transExp(absyn.AssignExp e, temp.AtomicLabel breakLabel){
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
	ExpTy transExp(absyn.IfExp e, temp.AtomicLabel breakLabel){
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
	ExpTy transExp(absyn.WhileExp e, temp.AtomicLabel breakLabel){
		ExpTy test = transExp(e.test, breakLabel);
		if(!test.ty.coerceTo(INT))
			error(e.pos, "integer required");
		temp.AtomicLabel finish = new temp.AtomicLabel();
		ExpTy body = transExp(e.body, finish);
		if(!body.ty.coerceTo(VOID))
			error(e.pos, "no value should be produced by the body of a while loop");
		return new ExpTy(translate.createWhileExp(test.exp, body.exp, finish), VOID);
	}
	ExpTy transExp(absyn.ForExp e, temp.AtomicLabel breakLabel){
		ExpTy hi = transExp(e.hi, breakLabel);
		if(!hi.ty.coerceTo(INT)){
			error(e.pos, "upper bound should be integer");
		}
		ExpTy init = transExp(e.var.init, breakLabel);
		if(!init.ty.coerceTo(INT)){
			error(e.pos, "lower bound should be integer");
		}
		VarEntry loopVar = new VarEntry(level.allocLocal(e.var.escape), INDEX);
		temp.AtomicLabel finish = new temp.AtomicLabel();

		env.venv.beginScope();
		env.venv.put(e.var.name, loopVar);
		ExpTy body = transExp(e.body, finish);
		env.venv.endScope();
		if(!body.ty.coerceTo(VOID))
			error(e.pos, "no value should be produced by the body of a while loop");
		return new ExpTy(translate.createForExp(loopVar.access, init.exp, hi.exp, body.exp, level, finish), VOID);
	}
	ExpTy transExp(absyn.BreakExp e, temp.AtomicLabel breakLabel){
		if(breakLabel == null)
			error(e.pos, "break must be in a loop");
		return new ExpTy(translate.createBreakExp(breakLabel), VOID);
	}
	ExpTy transExp(absyn.LetExp e, temp.AtomicLabel breakLabel){
		env.venv.beginScope();
		env.tenv.beginScope();
		translate.ExpList dec = new translate.ExpList(null, null);
		translate.ExpList savedec = dec;
		for(absyn.DecList p = e.decs; p != null; p = p.tail){
			translate.Exp t = transDec(p.head, breakLabel);
			if(t != null){
				dec.tail = new translate.ExpList(t, null);
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
	ExpTy transExp(absyn.ArrayExp e, temp.AtomicLabel breakLabel){
		types.Type ty = (types.Type)(env.tenv.get(e.typ));
		if(ty != null && ty.actual() instanceof types.ARRAY){
			ExpTy size = transExp(e.size, breakLabel);
			if(!size.ty.coerceTo(INT)){
				error(e.pos, "size should be an integer");
				return new ExpTy(null, ty);
			}
			ExpTy init = transExp(e.init, breakLabel);
			if(!init.ty.coerceTo(((types.ARRAY)ty.actual()).element)){
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
	Exp transDec(absyn.Dec e, temp.AtomicLabel breakLabel){
		if(e instanceof absyn.VarDec)return transDec((absyn.VarDec)e, breakLabel);
		else if(e instanceof absyn.FunctionDec)return transDec((absyn.FunctionDec)e, breakLabel);
		else if(e instanceof absyn.TypeDec)return transDec((absyn.TypeDec)e, breakLabel);
		else throw new Error("transDec");
	}
	Exp transDec(absyn.VarDec e, temp.AtomicLabel breakLabel){
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
			types.Type ty = transTy(e.typ);
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
	Exp transDec(absyn.TypeDec e, temp.AtomicLabel breakLabel){
		if(checkSame(e)){
			error(e.pos, "same type-id in a sequence");
			return null;
		}
		for(absyn.TypeDec p = e; p != null; p = p.next){
			env.tenv.put(p.name, new types.NAME(p.name));
		}
		for(absyn.TypeDec p = e; p != null; p = p.next){
			types.NAME name = (types.NAME)(env.tenv.get(p.name));
			name.bind(transTy(p.ty));
		}
		for(absyn.TypeDec p = e; p != null; p = p.next){
			types.NAME name = (types.NAME)(env.tenv.get(p.name));
			if(name.isLoop())
				error(p.pos, "type recursion cycle must pass through a record or array type");
		}
		return null;
	}
	Exp transDec(absyn.FunctionDec e, temp.AtomicLabel breakLabel){
		if(checkSame(e)){
			error(e.pos, "same function-id in a sequence");
			return null;
		}
		for(absyn.FunctionDec p = e; p != null; p = p.next){
			types.Type result = transTy(p.result);
			types.RECORD formals = transTypeField(p.params);
			temp.AtomicLabel label = new temp.AtomicLabel(p.name, true);
			util.BoolList escape = null;
			if(p.params != null)escape = p.params.getEscape();
			translate.Level newLevel = new translate.Level(level, label, escape, p.outGoing);
			env.venv.put(p.name, new FunEntry(newLevel, label, formals, result));
		}

		for(absyn.FunctionDec p = e; p != null; p = p.next){
			env.venv.beginScope();
			env.tenv.beginScope();
			FunEntry function = (FunEntry)env.venv.get(p.name);
			translate.AccessList formalAccess = function.level.getFormals();
			for(absyn.FieldList q = p.params; q != null; q = q.tail){
				types.Type ty = (types.Type)(env.tenv.get(q.typ));
				if(ty != null){
					env.venv.put(q.name, new VarEntry(formalAccess.head, ty));
					formalAccess = formalAccess.tail;
				}
				else 
					error(q.pos, "undefined type");
			}
			Semant newSemant = new Semant(env, translate, function.level);
			ExpTy body = newSemant.transExp(p.body, null);
			types.Type result = transTy(p.result);
			if(!body.ty.coerceTo(result))
				error(p.pos, "return type of " + p.name + " different from declaration");
			if(result.coerceTo(VOID))translate.procEntryExit(function.level, body.exp, true);
			else translate.procEntryExit(function.level, body.exp, false);
			env.venv.endScope();
			env.tenv.endScope();
		}
		return null;
	}
	types.Type transTy(absyn.Ty e){
		if(e instanceof absyn.NameTy)return transTy((absyn.NameTy)e);
		else if(e instanceof absyn.RecordTy)return transTy((absyn.RecordTy)e);
		else if(e instanceof absyn.ArrayTy)return transTy((absyn.ArrayTy)e);
		else throw new Error("transTy");
	}
	types.Type transTy(absyn.NameTy e){
		if(e == null)return VOID;
		types.Type name = (types.Type)(env.tenv.get(e.name));
		if(name == null){
			error(e.pos, "undefined type");
		}
		return name;
	}
	types.RECORD transTy(absyn.RecordTy e){
		types.RECORD t = transTypeField(e.fields);
		if(t == null)
			return new types.RECORD(null, null, null);
		else return t;

	}
	types.ARRAY transTy(absyn.ArrayTy e){
		types.Type name = (types.Type)(env.tenv.get(e.typ));
		if(name ==  null){
			error(e.pos, "undefined type");
		}
		return new types.ARRAY(name);
	}
	types.RECORD transTypeField(absyn.FieldList e){
		if(e == null)return null;
		if(checkSame(e)){
			error(e.pos,"same field name in a record");
		}
		types.Type typ = (types.Type)(env.tenv.get(e.typ));
		if(typ == null)
			error(e.pos, "undefined type " + e.typ.toString());
		types.RECORD r = new types.RECORD(e.name, typ, null);
		types.RECORD t = r;
		for(absyn.FieldList p = e.tail; p != null; p = p.tail){
			types.Type ty = (types.Type)(env.tenv.get(p.typ));
			if(ty == null)error(p.pos, "undefined type " + p.typ.toString());
			t.tail = new types.RECORD(p.name, ty, null);
			t = t.tail;
		}
		return r;

	}
	boolean checkSame(absyn.TypeDec e){
		for(absyn.TypeDec p = e.next; p != null; p = p.next){
			if(p.name == e.name)return true;
		}
		if(e.next == null)return false;
		else return checkSame(e.next);
	}
	boolean checkSame(absyn.FunctionDec e){
		for(absyn.FunctionDec p = e.next; p != null; p = p.next){
			if(p.name == e.name)return true;
		}
		if(e.next == null)return false;
		else return checkSame(e.next);
	}
	boolean checkSame(absyn.FieldList e){
		for(absyn.FieldList p = e.tail; p != null; p = p.tail){
			if(p.name == e.name)return true;
		}
		if(e.tail == null)return false;
		else return checkSame(e.tail);
	}
}