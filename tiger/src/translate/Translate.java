package translate;
import semant.ExpTy;
import semant.ExpTyList;
import semant.FunEntry;

public class Translate{
	Frag frags;
	private tree.Temp TEMP(temp.AtomicTemp t){
		return new tree.Temp(t);
	}
	
	public Exp simpleVar(Access var, Level level){
		return new Ex(var.exp(level.getFPOf(var.home)));
	}

	public Exp fieldVar(Exp var, int index, Level level){
		return new Ex(new tree.Mem(new tree.BinOp(tree.BinOp.PLUS, var.unEx(),
												  new tree.Const(index * level.frame.wordSize()) )));
	}

	public Exp subscriptVar(Exp var, Exp index, Level level){
		return new Ex(new tree.Mem(new tree.BinOp(tree.BinOp.PLUS, var.unEx(),
									new tree.BinOp(tree.BinOp.MUL, index.unEx(),
   								     new tree.Const(level.frame.wordSize())) )));
	}

	public Exp createIntExp(int value){
		return new IntExp(value);
	}

	public Exp createNilExp(){
		return new Ex(new tree.Const(0));
	}
	public Exp createArithExp(int binop, Exp left, Exp right){
		return new Ex(new tree.BinOp(binop, left.unEx(), right.unEx()));
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

			tree.ExpList argsTreeExp = null;
			tree.ExpList argsPoint = null;
			temp.AtomicTemp[] formal;
			formal = new temp.AtomicTemp[4];
			formal[0] = new temp.AtomicTemp();
			formal[1] = new temp.AtomicTemp();
			formal[2] = new temp.AtomicTemp();
			formal[3] = new temp.AtomicTemp();
			tree.Stm moveReg = null;
			int formalReg = 0;
			while(args != null){
				tree.Move move = new tree.Move(TEMP(formal[formalReg]), args.head.unEx());
				if(moveReg == null){
					moveReg = move;
					argsTreeExp = new tree.ExpList(TEMP(level.frame.FORMAL(formalReg)), null);
					argsPoint = argsTreeExp;
				}
				else {
					moveReg = new tree.Seq(moveReg, move);
					argsPoint.tail = new tree.ExpList(TEMP(level.frame.FORMAL(formalReg)), null);
					argsPoint = argsPoint.tail;
				}
				args = args.tail;
				formalReg++;
			}
			for(int i = 0; i < formalReg; i++)
				moveReg = new tree.Seq(moveReg, new tree.Move(TEMP(level.frame.FORMAL(i)), TEMP(formal[i])));
			/*			if(func.result.actual() instanceof Types.VOID)
						return new Nx(new Tree.SEQ(moveReg, level.frame.externalCall(func.label.toString(), argsTreeExp)));
			else */
			if (moveReg != null)
				return new Ex(new tree.Eseq(moveReg, level.frame.externalCall(func.label.toString(), argsTreeExp)));
			else return new Ex(level.frame.externalCall(func.label.toString(), argsTreeExp));
		}
		else{
			tree.ExpList argsTreeExp = new tree.ExpList(level.getFPOf(func.level.parent), null);
			tree.ExpList argsPoint = argsTreeExp;
			frame.Frame calleeframe = func.level.frame;
			frame.AccessList formalsAccess = calleeframe.getFormals();
			tree.Stm moveReg = new tree.Move(new tree.Mem(new tree.Temp(level.frame.SP())), argsTreeExp.head);

			temp.AtomicTemp[] formal;
			formal = new temp.AtomicTemp[4];
			formal[0] = new temp.AtomicTemp();
			formal[1] = new temp.AtomicTemp();
			formal[2] = new temp.AtomicTemp();
			formal[3] = new temp.AtomicTemp();

			formalsAccess = formalsAccess.tail;
			int formalReg = 0;
			int argNum = 1;
			while(args != null){
				tree.Move move;
				if(!formalsAccess.head.escape() && formalReg < 4){
					move = new tree.Move(TEMP(formal[formalReg]), args.head.unEx());
					argsPoint.tail = new tree.ExpList(TEMP(level.frame.FORMAL(formalReg)), null);
					argsPoint = argsPoint.tail;
					formalReg++;
				}
				else {
					tree.Exp address = new tree.BinOp(tree.BinOp.PLUS, new tree.Temp(level.frame.SP()), new tree.Const(argNum*level.frame.wordSize()));
					move = new tree.Move(new tree.Mem(address), args.head.unEx());
					//argsPoint.tail = new Tree.ExpList(formalsAccess.head.exp(TEMP(level.frame.SP())), null);
				}
				argNum++;
				formalsAccess = formalsAccess.tail;
				moveReg = new tree.Seq(moveReg, move);
				args = args.tail;
			}
			for(int i = 0; i < formalReg; i++)
				moveReg = new tree.Seq(moveReg, new tree.Move(TEMP(level.frame.FORMAL(i)), TEMP(formal[i])));
			
			/*			if(func.result.actual() instanceof Types.VOID)
						return new Nx(new SEQ(moveReg, new Tree.CALL(new Tree.NAME(func.label), argsTreeExp)));
						else */
			return new Ex(new tree.Eseq(moveReg,new tree.Call(new tree.Name(func.label), argsTreeExp.tail)));
		}
	}
	public Exp createExpList(ExpList e, boolean stm){
		if(e == null)return new Ex(new tree.Const(0));
		tree.Seq tseq = new tree.Seq(null, null);
		tree.Seq saveseq = tseq;
		if(stm){
			tree.Stm stms = e.head.unNx();
			e = e.tail;
			while(e != null){
				stms = new tree.Seq(stms, e.head.unNx());
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
				tree.Stm stms = e.head.unNx();
				e = e.tail;
				while(e.tail != null){
					stms = new tree.Seq(stms, e.head.unNx());
					e = e.tail;

				}
				return new Ex(new tree.Eseq(stms ,e.head.unEx()));
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
		return new Ex(new tree.Name( ((DataFrag)frags).label ));
	}

	public Exp createRecordExp(int count, ExpList init, Level level){
		tree.Stm stm;
		temp.AtomicTemp base = new temp.AtomicTemp();
		temp.AtomicTemp point = new temp.AtomicTemp();
		stm = new tree.Move(new tree.Temp(level.frame.FORMAL(0)), new tree.Const(count*4));
		stm = new tree.Seq(stm, new tree.Move(new tree.Temp(base), 
											  level.frame.externalCall("malloc", 
																	   new tree.ExpList(new tree.Temp(level.frame.FORMAL(0)), null))));
		stm = new tree.Seq(stm, new tree.Move(new tree.Temp(point), new tree.Temp(base)));
		while(init != null){
			stm = new tree.Seq(stm, new tree.Move(new tree.Mem(new tree.Temp(point)), 
												  init.head.unEx()));
			stm = new tree.Seq(stm, new tree.Move(new tree.Temp(point), 
												  new tree.BinOp(tree.BinOp.PLUS, 
																 new tree.Temp(point), 
																 new tree.Const(level.frame.wordSize()))));
			init = init.tail;
		}
		Ex ans =  new Ex(new tree.Eseq(stm, new tree.Temp(base)));
		ans.high = true;
		return ans;
	}

	public Exp createArrayExp(Exp size, Exp init, absyn.Exp initAbsyn, Level level){
		tree.Stm stm;
		//			Temp.Temp size = new Temp.Temp();
		temp.AtomicTemp base = new temp.AtomicTemp();
		temp.AtomicTemp point = new temp.AtomicTemp();
		temp.AtomicTemp end = new temp.AtomicTemp();
		temp.AtomicTemp initValue = new temp.AtomicTemp();
		temp.AtomicLabel begin = new temp.AtomicLabel();
		temp.AtomicLabel finish = new temp.AtomicLabel();
			

		stm = new tree.Move(new tree.Temp(level.frame.FORMAL(0)),new tree.BinOp(tree.BinOp.MUL, size.unEx(),new tree.Const(level.frame.wordSize())));
		stm = new tree.Seq(stm, new tree.Move(new tree.Temp(base), 
											  level.frame.externalCall("malloc", 
																	   new tree.ExpList(new tree.Temp(level.frame.FORMAL(0)), null))));
		tree.Exp left = new tree.BinOp(tree.BinOp.PLUS, 
									   new tree.Temp(level.frame.FORMAL(0)), 
									   new tree.Temp(base));
		stm = new tree.Seq(stm, new tree.Move(new tree.Temp(end), left));
		if(!init.high)
			stm = new tree.Seq(stm, new tree.Move(new tree.Temp(initValue), init.unEx()));
		stm = new tree.Seq(stm , new tree.Move(new tree.Temp(point), new tree.Temp(base)));
		stm = new tree.Seq(stm, new tree.Label(begin));
		if(init.high)
			stm = new tree.Seq(stm, new tree.Move(new tree.Mem(new tree.Temp(point)), init.unEx()));
		else 
			stm = new tree.Seq(stm, new tree.Move(new tree.Mem(new tree.Temp(point)), new tree.Temp(initValue)));

		stm = new tree.Seq(stm, new tree.Move(new tree.Temp(point),
											  new tree.BinOp(tree.BinOp.PLUS, 
															 new tree.Temp(point),
															 new tree.Const(level.frame.wordSize()))));

		stm = new tree.Seq(stm, new tree.Cjump(tree.Cjump.EQ, new tree.Temp(end), new tree.Temp(point),
											   finish, begin));
		stm = new tree.Seq(stm, new tree.Jump(begin));
		stm = new tree.Seq(stm, new tree.Label(finish));
		Ex ans =  new Ex(new tree.Eseq(stm, new tree.Temp(base)));
		ans.high = true;
		return ans;
	}
	public Exp createWhileExp(Exp test, Exp body, temp.AtomicLabel done){
		temp.AtomicLabel begin = new temp.AtomicLabel();
		temp.AtomicLabel cont = new temp.AtomicLabel();
		tree.Seq seq = new tree.Seq(new tree.Label(begin), 
						new tree.Seq(test.unCx(cont,done), 
						 new tree.Seq(new tree.Label(cont), 
						  new tree.Seq(body.unNx(), 
						   new tree.Seq(new tree.Jump(begin),
							new tree.Label(done))))));
		return new Nx(seq);
	}

	public Exp createForExp(Access loopVar, Exp low, Exp hi, Exp body, Level level, temp.AtomicLabel finish){
		temp.AtomicTemp limit = new temp.AtomicTemp();
		//		Tree.Exp var = loopVar.exp(new Tree.TEMP(level.frame.FP()));
		// !! Attention !! "var" below refers to the same Tree.Exp
		//Temp.Label finish = new Temp.Label();
		temp.AtomicLabel start = new temp.AtomicLabel();
		temp.AtomicLabel plus = new temp.AtomicLabel();

		tree.Stm a;
		a = new tree.Move(loopVar.exp(new tree.Temp(level.frame.FP())), low.unEx());
		a = new tree.Seq(a, new tree.Move(new tree.Temp(limit), hi.unEx()));
		a = new tree.Seq(a, 
					new tree.Cjump(tree.Cjump.GT, loopVar.exp(new tree.Temp(level.frame.FP())), new tree.Temp(limit), finish, start));
		a = new tree.Seq(a, new tree.Label(start));
		a = new tree.Seq(a, body.unNx());
		a = new tree.Seq(a, new tree.Cjump(tree.Cjump.EQ, loopVar.exp(new tree.Temp(level.frame.FP())), new tree.Temp(limit), finish, plus));
		a = new tree.Seq(a, new tree.Label(plus));
		a = new tree.Seq(a, new tree.Move(loopVar.exp(new tree.Temp(level.frame.FP())),
										  new tree.BinOp(tree.BinOp.PLUS, loopVar.exp(new tree.Temp(level.frame.FP())), new tree.Const(1))));
		a = new tree.Seq(a, new tree.Jump(start));
		a = new tree.Seq(a, new tree.Label(finish));
		return new Nx(a);
	}
	
	public Exp createBreakExp(temp.AtomicLabel finish){
		return new Nx(new tree.Jump(finish));
	}

	public Exp createAssignExp(Exp var, Exp exp){
		return new Nx(new tree.Move(var.unEx(), exp.unEx()));
	}

	public Exp createLetExp(ExpList dec, Exp body, boolean ex){
		if(dec == null)return body;
		tree.Stm stm = dec.head.unNx();
		dec = dec.tail;
		while(dec != null){
			stm = new tree.Seq(stm, dec.head.unNx());
			dec = dec.tail;
		}
		if(ex)return new Ex(new tree.Eseq(stm, body.unEx()));
		else return new Nx(new tree.Seq(stm, body.unNx()));
	}

	public Exp createVarDec(Access var, Exp init, Level level){
		return new Nx(new tree.Move(var.exp(new tree.Temp(level.frame.FP())), init.unEx()));
	}
	public void procEntryExit(Level level, Exp body, boolean process){
		if(process)frags = new ProcFrag(level.frame.procEntryExit1(body.unNx()), level.frame, frags);
		else frags = new ProcFrag(level.frame.procEntryExit1(new tree.Move(new tree.Temp(level.frame.RV()), body.unEx())), level.frame, frags);
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
