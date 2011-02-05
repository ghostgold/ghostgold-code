import Translate.Exp;
public class Semant
{
	Env env;
	public Semant(ErrorMsg.ErrorMsg err){this(new Env(err));}
	public Semant(Env e){env = e;}
	public void transProg(Absyn.Exp exp);
	ExpTy transVar(Absyn.Var e){
		if(e instanceof Absyn.SimpleVar)return transVar((Absyn.SimpleVar)e);
		else if (e instanceof Absyn.FieldVar)return transVar((Absyn.FieldVar)e);
		else if(e instanceof Absyn.SubscriptVar)return transVar((Absyn.SbuscriptVar)e);
		throw new Error("transVar");
	}
	ExpTy transVar(Absyn.SimpleVar e){
		Entry x = (Entry)env.venv.get(e.name);
		if(x instanceof VarEntry){
			VarEntry ent = (VarEntry)x;
			return new ExpTy(null, ent.ty);
		}
		else {
			error(v.pos, "undefined variable");
			return new ExpTy(null, UNKNOWN);
		}
	}
	ExpTy transVar(Absyn.FieldVar e){
		ExpTy var = transVar(e.var);
		if(var.ty.actual() instanceof Types.RECORD){
			Types.RECORD type = (Types.RECORD)var.ty;
			Type f = type.check(e.field);
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
			Expty index = transExp(e.index);
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
				return new ExpTy(null, UNKNOWN);
			}
		}
	}
	ExpTy transExp(Absyn.OpExp e){
		ExpTy left = transExp(e.left);
		ExpTy right = transExp(e.right);
		switch(e.oper){
		Absyn.OpExp.PLUS:
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
		Absyn.OpExp.MINUS:
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
		Absyn.OpExp.MUL:
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
		Absyn.OpExp.DIV:
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
		Absyn.OpExp.EQ:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(right.ty) || right.ty.coerceTo(left.ty))
				return new ExpTy(null, INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING))){
				error(e.left.pos, "integer or string required");
				return new ExpTy(null, INT);
			}
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING))){
				error(e.right.pos, "integer required");
			}
			break;
		Absyn.OpExp.NE:
			if(left.ty.coerceTo(INT) && right.ty.coerceTo(INT))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(STRING) && right.ty.coerceTo(STRING))
				return new ExpTy(null, INT);
			else if(left.ty.coerceTo(right.ty) || right.ty.coerceTo(left.ty))
				return new ExpTy(null, INT);
			else if(!(left.ty.coerceTo(INT) || left.ty.coerceTo(STRING))){
				error(e.left.pos, "integer or string required");
				return new ExpTy(null, INT);
			}
			else if(!(right.ty.coerceTo(INT) || right.ty.coerceTo(STRING))){
				error(e.right.pos, "integer required");
			}
			break;
		Absyn.OpExp.LT:
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
		Absyn.OpExp.LE:
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
		Absyn.OpExp.GT:
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
		Absyn.OpExp.GE:
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
		}
	}
	ExpTy transExp(Absyn.RecordExp e){
		Types.Type r = (Types.Type)env.tenv.get(e.typ);
		if(r instanceof Types.RECORD){
			f = e.fields;
			save = (Types.RECORD)r
			r = save;
			while(f != null && r != null){
				if(f.name != r.fieldname){
					error(f.pos, "record " + e.typ.toString() + " has no field with name" 
						  + f.name.toString());
				}
				ExpTy init = transExp(f.init);
				if(!init.ty.coerceTo(r.fieldType)){
					error(f.pos, "type of exp is different with the type of " 
						  + r.fieldname.toString() + " in declaration");
				}
			}
			if(f != null)
				error("too many fields");
			else if(r != null)
				error("the fields is not enough");
			return new ExpTy(null, save);
		}
		else {
			error(e.pos, "undefined type");
			return new ExpTy(null, UNKNOWN);
		}
	}
	Exp transDec(Absyn.Dec e){}
	Ty transTy(Absyn.Ty e){}
	
}