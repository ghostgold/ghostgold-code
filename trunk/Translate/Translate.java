package Translate;
import Semant.ExpTy;
import Semant.ExpTyList;
import Semant.FunEntry;

public class Translate{
	Frag frags;
	private Tree.TEMP TEMP(Temp.Temp t){
		return new Tree.TEMP(t);
	}
	
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
	public Exp createArithExp(int binop, Exp left, Exp right){
		return new Ex(new Tree.BINOP(binop, left.unEx(), right.unEx()));
	}
	public Exp createCompareExp(int rel, Exp left, Exp right){
		return new RelCx(rel, left.unEx(), right.unEx());
	}
	public Exp createStringCompareExp(int rel, Exp left, Exp right, Level level){
		return new StringRelCx(rel, left.unEx(), right.unEx(), level);
	}
	public Exp createCallExp(FunEntry func, ExpList args, Level level){
		//different between system call or user func

		if(func.level.frame.getName().toString().equals("main")){

			Tree.ExpList argsTreeExp = null;
			Tree.ExpList argsPoint = null;
			Temp.Temp[] formal;
			formal = new Temp.Temp[4];
			formal[0] = new Temp.Temp();
			formal[1] = new Temp.Temp();
			formal[2] = new Temp.Temp();
			formal[3] = new Temp.Temp();
			Tree.Stm moveReg = null;
			int formalReg = 0;
			while(args != null){
				Tree.MOVE move = new Tree.MOVE(TEMP(formal[formalReg]), args.head.unEx());
				if(moveReg == null){
					moveReg = move;
					argsTreeExp = new Tree.ExpList(TEMP(level.frame.FORMAL(formalReg)), null);
					argsPoint = argsTreeExp;
				}
				else {
					moveReg = new Tree.SEQ(moveReg, move);
					argsPoint.tail = new Tree.ExpList(TEMP(level.frame.FORMAL(formalReg)), null);
					argsPoint = argsPoint.tail;
				}
				args = args.tail;
				formalReg++;
			}
			for(int i = 0; i < formalReg; i++)
				moveReg = new Tree.SEQ(moveReg, new Tree.MOVE(TEMP(level.frame.FORMAL(i)), TEMP(formal[i])));
			/*			if(func.result.actual() instanceof Types.VOID)
						return new Nx(new Tree.SEQ(moveReg, level.frame.externalCall(func.label.toString(), argsTreeExp)));
			else */
			if (moveReg != null)
				return new Ex(new Tree.ESEQ(moveReg, level.frame.externalCall(func.label.toString(), argsTreeExp)));
			else return new Ex(level.frame.externalCall(func.label.toString(), argsTreeExp));
		}
		else{
			Tree.ExpList argsTreeExp = new Tree.ExpList(level.getFPOf(func.level.parent), null);
			Tree.ExpList argsPoint = argsTreeExp;
			Frame.Frame calleeframe = func.level.frame;
			Frame.AccessList formalsAccess = calleeframe.getFormals();
			Tree.Stm moveReg = new Tree.MOVE(new Tree.MEM(new Tree.TEMP(level.frame.SP())), argsTreeExp.head);

			Temp.Temp[] formal;
			formal = new Temp.Temp[4];
			formal[0] = new Temp.Temp();
			formal[1] = new Temp.Temp();
			formal[2] = new Temp.Temp();
			formal[3] = new Temp.Temp();

			formalsAccess = formalsAccess.tail;
			int formalReg = 0;
			int argNum = 1;
			while(args != null){
				Tree.MOVE move;
				if(!formalsAccess.head.escape() && formalReg < 4){
					move = new Tree.MOVE(TEMP(formal[formalReg]), args.head.unEx());
					argsPoint.tail = new Tree.ExpList(TEMP(level.frame.FORMAL(formalReg)), null);
					argsPoint = argsPoint.tail;
					formalReg++;
				}
				else {
					Tree.Exp address = new Tree.BINOP(Tree.BINOP.PLUS, new Tree.TEMP(level.frame.SP()), new Tree.CONST(argNum*level.frame.wordSize()));
					move = new Tree.MOVE(new Tree.MEM(address), args.head.unEx());
					//argsPoint.tail = new Tree.ExpList(formalsAccess.head.exp(TEMP(level.frame.SP())), null);
				}
				argNum++;
				formalsAccess = formalsAccess.tail;
				moveReg = new Tree.SEQ(moveReg, move);
				args = args.tail;
			}
			for(int i = 0; i < formalReg; i++)
				moveReg = new Tree.SEQ(moveReg, new Tree.MOVE(TEMP(level.frame.FORMAL(i)), TEMP(formal[i])));
			
			/*			if(func.result.actual() instanceof Types.VOID)
						return new Nx(new SEQ(moveReg, new Tree.CALL(new Tree.NAME(func.label), argsTreeExp)));
						else */
			return new Ex(new Tree.ESEQ(moveReg,new Tree.CALL(new Tree.NAME(func.label), argsTreeExp.tail)));
		}
	}
	public Exp createExpList(ExpList e, boolean stm){
		if(e == null)return new Ex(new Tree.CONST(0));
		Tree.SEQ tseq = new Tree.SEQ(null, null);
		Tree.SEQ saveseq = tseq;
		if(stm){
			Tree.Stm stms = e.head.unNx();
			e = e.tail;
			while(e != null){
				stms = new Tree.SEQ(stms, e.head.unNx());
				e = e.tail;
			}
			/*			if(e.tail != null){
				//	return new Nx(new Tree.SEQ(e.head.unNx(), createExpList(e.tail, true).unNx()));
				/*{while(e != null){
				tseq.right = new Tree.SEQ(e.head.unNx(), null);
				e = e.tail;
				tseq = (Tree.SEQ)tseq.right;
				}
			}*/
			return new Nx(stms);
		}
		else {
			if(e.tail !=null){
				Tree.Stm stms = e.head.unNx();
				e = e.tail;
				while(e.tail != null){
					stms = new Tree.SEQ(stms, e.head.unNx());
					e = e.tail;

				}
				return new Ex(new Tree.ESEQ(stms ,e.head.unEx()));
			}
			else{
				return e.head;
			}
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
		stm = new Tree.MOVE(new Tree.TEMP(level.frame.FORMAL(0)), new Tree.CONST(count*4));
		stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.TEMP(base), 
											  level.frame.externalCall("malloc", 
																	   new Tree.ExpList(new Tree.TEMP(level.frame.FORMAL(0)), null))));
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
		Ex ans =  new Ex(new Tree.ESEQ(stm, new Tree.TEMP(base)));
		ans.high = true;
		return ans;
	}

	public Exp createArrayExp(Exp size, Exp init, Absyn.Exp initAbsyn, Level level){
		Tree.Stm stm;
		//			Temp.Temp size = new Temp.Temp();
		Temp.Temp base = new Temp.Temp();
		Temp.Temp point = new Temp.Temp();
		Temp.Temp end = new Temp.Temp();
		Temp.Temp initValue = new Temp.Temp();
		Temp.Label begin = new Temp.Label();
		Temp.Label finish = new Temp.Label();
			

		stm = new Tree.MOVE(new Tree.TEMP(level.frame.FORMAL(0)),new Tree.BINOP(Tree.BINOP.MUL, size.unEx(),new Tree.CONST(level.frame.wordSize())));
		stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.TEMP(base), 
											  level.frame.externalCall("malloc", 
																	   new Tree.ExpList(new Tree.TEMP(level.frame.FORMAL(0)), null))));
		Tree.Exp left = new Tree.BINOP(Tree.BINOP.PLUS, 
									   new Tree.TEMP(level.frame.FORMAL(0)), 
									   new Tree.TEMP(base));
		stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.TEMP(end), left));
		if(!init.high)
			stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.TEMP(initValue), init.unEx()));
		stm = new Tree.SEQ(stm , new Tree.MOVE(new Tree.TEMP(point), new Tree.TEMP(base)));
		stm = new Tree.SEQ(stm, new Tree.LABEL(begin));
		if(init.high)
			stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.MEM(new Tree.TEMP(point)), init.unEx()));
		else 
			stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.MEM(new Tree.TEMP(point)), new Tree.TEMP(initValue)));

		stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.TEMP(point),
											  new Tree.BINOP(Tree.BINOP.PLUS, 
															 new Tree.TEMP(point),
															 new Tree.CONST(level.frame.wordSize()))));

		stm = new Tree.SEQ(stm, new Tree.CJUMP(Tree.CJUMP.EQ, new Tree.TEMP(end), new Tree.TEMP(point),
											   finish, begin));
		stm = new Tree.SEQ(stm, new Tree.JUMP(begin));
		stm = new Tree.SEQ(stm, new Tree.LABEL(finish));
		Ex ans =  new Ex(new Tree.ESEQ(stm, new Tree.TEMP(base)));
		ans.high = true;
		return ans;
	}
	public Exp createWhileExp(Exp test, Exp body, Temp.Label done){
		Temp.Label begin = new Temp.Label();
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
		//		Tree.Exp var = loopVar.exp(new Tree.TEMP(level.frame.FP()));
		// !! Attention !! "var" below refers to the same Tree.Exp
		//Temp.Label finish = new Temp.Label();
		Temp.Label start = new Temp.Label();
		Temp.Label plus = new Temp.Label();

		Tree.Stm a;
		a = new Tree.MOVE(loopVar.exp(new Tree.TEMP(level.frame.FP())), low.unEx());
		a = new Tree.SEQ(a, new Tree.MOVE(new Tree.TEMP(limit), hi.unEx()));
		a = new Tree.SEQ(a, 
					new Tree.CJUMP(Tree.CJUMP.GT, loopVar.exp(new Tree.TEMP(level.frame.FP())), new Tree.TEMP(limit), finish, start));
		a = new Tree.SEQ(a, new Tree.LABEL(start));
		a = new Tree.SEQ(a, body.unNx());
		a = new Tree.SEQ(a, new Tree.CJUMP(Tree.CJUMP.EQ, loopVar.exp(new Tree.TEMP(level.frame.FP())), new Tree.TEMP(limit), finish, plus));
		a = new Tree.SEQ(a, new Tree.LABEL(plus));
		a = new Tree.SEQ(a, new Tree.MOVE(loopVar.exp(new Tree.TEMP(level.frame.FP())),
										  new Tree.BINOP(Tree.BINOP.PLUS, loopVar.exp(new Tree.TEMP(level.frame.FP())), new Tree.CONST(1))));
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

	public Exp createLetExp(ExpList dec, Exp body, boolean ex){
		if(dec == null)return body;
		Tree.Stm stm = dec.head.unNx();
		dec = dec.tail;
		while(dec != null){
			stm = new Tree.SEQ(stm, dec.head.unNx());
			dec = dec.tail;
		}
		if(ex)return new Ex(new Tree.ESEQ(stm, body.unEx()));
		else return new Nx(new Tree.SEQ(stm, body.unNx()));
	}

	public Exp createVarDec(Access var, Exp init, Level level){
		return new Nx(new Tree.MOVE(var.exp(new Tree.TEMP(level.frame.FP())), init.unEx()));
	}
	public void procEntryExit(Level level, Exp body, boolean process){
		if(process)frags = new ProcFrag(level.frame.procEntryExit1(body.unNx()), level.frame, frags);
		else frags = new ProcFrag(level.frame.procEntryExit1(new Tree.MOVE(new Tree.TEMP(level.frame.RV()), body.unEx())), level.frame, frags);
	}

	public Frag getResults(){
		return frags;
	}
}
	// public Exp createMinusExp(Exp left, Exp right){
	// 	return new Ex(new Tree.BINOP(Tree.BINOP.MINUS, left.unEx(), right.unEx()));
	// }
	// public Exp createMulExp(Exp left, Exp right){
	// 	return new Ex(new Tree.BINOP(Tree.BINOP.MUL, left.unEx(), right.unEx()));
	// }
	// public Exp createDivExp(Exp left, Exp right){
	// 	return new Ex(new Tree.BINOP(Tree.BINOP.DIV, left.unEx(), right.unEx()));
	// }
	// public Exp createNeExp(Exp left, Exp right){
	// 	return new RelCx(Tree.CJUMP.NE, left.unEx(), right.unEx());
	// }
	// public Exp createLtExp(Exp left, Exp right){
	// 	return new RelCx(Tree.CJUMP.LT, left.unEx(), right.unEx());
	// }
	// public Exp createLeExp(Exp left, Exp right){
	// 	return new RelCx(Tree.CJUMP.LE, left.unEx(), right.unEx());
	// }
	// public Exp createGtExp(Exp left, Exp right){
	// 	return new RelCx(Tree.CJUMP.GT, left.unEx(), right.unEx());
	// }
	// public Exp createGeExp(Exp left, Exp right){
	// 	return new RelCx(Tree.CJUMP.GE, left.unEx(), right.unEx());
	// }
	// public Exp createStringNeExp(Exp left, Exp right, Level level){
	// 	return new StringRelCx(Tree.CJUMP.NE, left.unEx(), right.unEx(), level);
	// }
	// public Exp createStringLtExp(Exp left, Exp right, Level level){
	// 	return new StringRelCx(Tree.CJUMP.LT, left.unEx(), right.unEx(), level);
	// }
	// public Exp createStringLeExp(Exp left, Exp right, Level level){
	// 	return new StringRelCx(Tree.CJUMP.LE, left.unEx(), right.unEx(), level);
	// }
	// public Exp createStringGtExp(Exp left, Exp right, Level level){
	// 	return new StringRelCx(Tree.CJUMP.GT, left.unEx(), right.unEx(), level);
	// }
	// public Exp createStringGeExp(Exp left, Exp right, Level level){
	// 	return new StringRelCx(Tree.CJUMP.GE, left.unEx(), right.unEx(), level);
	// }
			// Tree.Stm stm;
			// Temp.Temp size = new Temp.Temp();
			// Temp.Temp base = new Temp.Temp();
			// Temp.Temp value = new Temp.Temp();
			// //stm = new Tree.MOVE(new Tree.TEMP(size), sizeF.unEx());
			// //stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.TEMP(value), init.unEx()));
			// stm = new Tree.MOVE(new Tree.TEMP(base), 
			// 					level.frame.externalCall("allocSizeWithValue", 
			// 					 new Tree.ExpList(sizeF.unEx(), 
			// 					  new Tree.ExpList(init.unEx(), null))));
			// // stm = new Tree.SEQ(stm , new Tree.MOVE(new Tree.TEMP(point), new Tree.TEMP(base)));
			// // stm = new Tree.SEQ(stm, new Tree.LABEL(begin));
			// // stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.MEM(new Tree.TEMP(point)), init.unEx()));
			// // stm = new Tree.SEQ(stm, new Tree.MOVE(new Tree.TEMP(point),
			// // 						  new Tree.BINOP(Tree.BINOP.PLUS, 
			// // 										 new Tree.TEMP(point),
			// // 										 new Tree.CONST(level.frame.wordSize()))));
			// // Tree.Exp left = new Tree.BINOP(Tree.BINOP.PLUS, 
			// // 							   new Tree.BINOP(Tree.BINOP.MUL,
			// // 											  Tree.CONST(level.frame.wordSize()),
			// // 											  new Tree.TEMP(size)), 
			// // 							   new Tree.TEMP(base));
			// // stm = new Tree.SEQ(stm, new Tree.CJUMP(Tree.CJUMP.EQ, left, new Tree.TEMP(point),
			// // 									   finish, begin));
			// // stm = new Tree.SEQ(stm, new Tree.JUMP(begin));
			// // stm = new Tree.SEQ(stm, new Tree.LABEL(finish));
			// return new Ex(new Tree.ESEQ(stm, new Tree.TEMP(base)));
