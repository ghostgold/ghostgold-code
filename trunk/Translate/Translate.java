package Translate;
import Semant.ExpTy;
import Semant.ExpTyList;
import Semant.FunEntry;
public class Translate{
	Frag frags;

	public Exp simpleVar(Access var, Level level){
		return new Ex(var.exp(level.getFPOf(var.home)));
	}

	public Exp fieldVar(Exp var, int index, Level level){
		return new Ex(new Tree.MEM(new Tree.BINOP(Tree.BINOP.PLUS, var.unEx(),
												  new Tree.CONST(index * level.frame.wordSize()) )));
	}

	public Exp subscriptVar(Exp var, Exp index, Level level){
		return new Ex(new Tree.MEM(new Tree.BINOP(Tree.BINOP.PLUS, var.unEx(),
									new Tree.BINOP(Tree.BINOP.MUL, index.unEx(),
   								     new Tree.CONST(level.frame.wordSize())) )));
	}

	public Exp createIntExp(int value){
		return new IntExp(value);
	}

	public Exp createNilExp(){
		return new Ex(new Tree.CONST(0));
	}
	public Exp createPlusExp(Exp left, Exp right){
		return new Ex(new Tree.BINOP(Tree.BINOP.PLUS, left.unEx(), right.unEx()));
	}
	public Exp createMinusExp(Exp left, Exp right){
		return new Ex(new Tree.BINOP(Tree.BINOP.MINUS, left.unEx(), right.unEx()));
	}
	public Exp createMulExp(Exp left, Exp right){
		return new Ex(new Tree.BINOP(Tree.BINOP.MUL, left.unEx(), right.unEx()));
	}
	public Exp createDivExp(Exp left, Exp right){
		return new Ex(new Tree.BINOP(Tree.BINOP.DIV, left.unEx(), right.unEx()));
	}
	public Exp createEqExp(Exp left, Exp right){
		return new RelCx(Tree.CJUMP.EQ, left.unEx(), right.unEx());
	}
	public Exp createNeExp(Exp left, Exp right){
		return new RelCx(Tree.CJUMP.NE, left.unEx(), right.unEx());
	}
	public Exp createLtExp(Exp left, Exp right){
		return new RelCx(Tree.CJUMP.LT, left.unEx(), right.unEx());
	}
	public Exp createLeExp(Exp left, Exp right){
		return new RelCx(Tree.CJUMP.LE, left.unEx(), right.unEx());
	}
	public Exp createGtExp(Exp left, Exp right){
		return new RelCx(Tree.CJUMP.GT, left.unEx(), right.unEx());
	}
	public Exp createGeExp(Exp left, Exp right){
		return new RelCx(Tree.CJUMP.GE, left.unEx(), right.unEx());
	}
	public Exp createStringEqExp(Exp left, Exp right, Level level){
		return new StringRelCx(Tree.CJUMP.EQ, left.unEx(), right.unEx(), level);
	}
	public Exp createStringNeExp(Exp left, Exp right, Level level){
		return new StringRelCx(Tree.CJUMP.NE, left.unEx(), right.unEx(), level);
	}
	public Exp createStringLtExp(Exp left, Exp right, Level level){
		return new StringRelCx(Tree.CJUMP.LT, left.unEx(), right.unEx(), level);
	}
	public Exp createStringLeExp(Exp left, Exp right, Level level){
		return new StringRelCx(Tree.CJUMP.LE, left.unEx(), right.unEx(), level);
	}
	public Exp createStringGtExp(Exp left, Exp right, Level level){
		return new StringRelCx(Tree.CJUMP.GT, left.unEx(), right.unEx(), level);
	}
	public Exp createStringGeExp(Exp left, Exp right, Level level){
		return new StringRelCx(Tree.CJUMP.GE, left.unEx(), right.unEx(), level);
	}
	public Exp createCallExp(FunEntry func, ExpList args, Level level){
		Tree.ExpList targs = new Tree.ExpList(level.getFPOf(func.level), null);
		Tree.ExpList saveargs = targs;
		while(args != null){
			targs.tail = new Tree.ExpList(args.head.unEx(), null);
			args = args.tail;
			targs = targs.tail;
		}
		return new Ex(new Tree.CALL(new Tree.NAME(func.label), saveargs));
	}
	public Exp createExpList(ExpList e, boolean stm){
		Tree.SEQ tseq = new Tree.SEQ(null, null);
		Tree.SEQ saveseq = tseq;
		if(stm){
			while(e != null){
				tseq.right = new Tree.SEQ(e.head.unNx(), null);
				e = e.tail;
				tseq = (Tree.SEQ)tseq.right;
			}
			return new Nx(saveseq.right);
		}
		else {
			while(e.tail != null){
				tseq.right = new Tree.SEQ(e.head.unNx(), null);
				e = e.tail;
				tseq = (Tree.SEQ)tseq.right;
			}
			return new Ex(new Tree.ESEQ(saveseq.right, e.head.unEx()));
		}
	}
	public Exp createIfThenElse(Exp test, Exp thenclause, Exp elseclause){
		return new IfThenElseExp(test, thenclause, elseclause);
	}
	public Exp createStringExp(String value){
		frags = new DataFrag(value, frags);
		return new Ex(new Tree.NAME( ((DataFrag)frags).label ));
	}

	public Exp createRecordExp(int count, ExpList init, Level level){
		Tree.Stm stm;
		Temp.Temp base = new Temp.Temp();
		Temp.Temp point = new Temp.Temp();
		stm = new Tree.MOVE(new Tree.TEMP(base), 
			   level.frame.externalCall("allocRecord", 
			    new Tree.ExpList(new Tree.CONST(count), null)));
		stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.TEMP(point), new Tree.TEMP(base)));
		while(init != null){
			stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.MEM(new Tree.TEMP(point)), 
												  init.head.unEx()));
			stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.TEMP(point), 
												  new Tree.BINOP(Tree.BINOP.PLUS, 
																 new Tree.TEMP(point), 
																 new Tree.CONST(level.frame.wordSize()))));
			init = init.tail;
		}
		return new Ex(new Tree.ESEQ(stm, new Tree.TEMP(base)));
	}

	public Exp createArrayExp(){
		
	}
	public Exp createWhileExp(Exp test, Exp body, Temp.Label done){
		Temp.Label begin = new Temp.Label();
		//		Temp.Label done = new Temp.Label();
		Temp.Label cont = new Temp.Label();
		Tree.SEQ seq = new Tree.SEQ(new Tree.LABEL(begin), 
						new Tree.SEQ(test.unCx(cont,done), 
						 new Tree.SEQ(new Tree.LABEL(cont), 
						  new Tree.SEQ(body.unNx(), 
						   new Tree.SEQ(new Tree.JUMP(begin),
							new Tree.LABEL(done))))));
		return new Nx(seq);
	}

	public Exp createForExp(Access loopVar, Exp low, Exp hi, Exp body, Level level, Temp.Label finish){
		Temp.Temp limit = new Temp.Temp();
		Tree.Exp var = loopVar.exp(new Tree.TEMP(level.frame.FP()));
		// !! Attention !! "var" below refers to the same Tree.Exp
		//Temp.Label finish = new Temp.Label();
		Temp.Label start = new Temp.Label();
		Temp.Label plus = new Temp.Label();

		Tree.Stm a;
		a = new Tree.MOVE(var, low.unEx());
		a = new Tree.SEQ(a, new Tree.MOVE(new Tree.TEMP(limit), hi.unEx()));
		a = new Tree.SEQ(a, 
					new Tree.CJUMP(Tree.CJUMP.GT, var, new Tree.TEMP(limit), finish, start));
		a = new Tree.SEQ(a, new Tree.LABEL(start));
		a = new Tree.SEQ(a, body.unNx());
		a = new Tree.SEQ(a, new Tree.CJUMP(Tree.CJUMP.EQ, var, new Tree.TEMP(limit), finish, plus));
		a = new Tree.SEQ(a, new Tree.LABEL(plus));
		a = new Tree.SEQ(a, new Tree.MOVE(var,
										  new Tree.BINOP(Tree.BINOP.PLUS, var, new Tree.CONST(1))));
		a = new Tree.SEQ(a, new Tree.JUMP(start));
		a = new Tree.SEQ(a, new Tree.LABEL(finish));
		return new Nx(a);
	}
	
	public Exp createBreakExp(Temp.Label finish){
		return new Nx(new Tree.JUMP(finish));
	}

	public Exp createAssignExp(Exp var, Exp exp){
		return new Nx(new Tree.MOVE(var.unEx(), exp.unEx()));
	}
}