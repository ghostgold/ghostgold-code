package Mips;
import Frame.Frame;
public class Codegen {
	Frame frame;
	public Codegen(Frame f){
		frame = f;
	}
	private Assem.InstrList ilist = null;
	private Assem.InstrList last = null;
	private void emit(Assem.Instr inst){
		if(last != null)
			last = last.tail = new Assem.InstrList(inst, null);
		else 
			last = ilist = new Assem.InstrList(inst, null);
	}
	private Temp.TempList L(Temp.Temp h, Temp.TempList t){
		return new Temp.TempList(h,t);
	}

	void munchStm(Tree.Stm s){
		if(s instanceof Tree.SEQ){
			Tree.SEQ seq = (Tree.SEQ)s;
			munchStm(seq.left);
			munchStm(seq.right);
			return;
		}
		if(s instanceof Tree.MOVE){
			munchStm((Tree.MOVE)s);
			return;
		}
		if(s instanceof Tree.LABEL){
			Tree.LABEL label = (Tree.LABEL)s;
			emit(new Assem.LABEL(label.label.toString() + ":", label.label));
			return;
		}
		if(s instanceof Tree.JUMP){
			emit(new Assem.JUMP("j `j0", null, null, ((Tree.JUMP)s).targets));
			return;
		}
		if(s instanceof Tree.CJUMP){
			Tree.CJUMP cjump = (Tree.CJUMP)s;
			String branch = "";
			switch(cjump.relop){
			case Tree.CJUMP.EQ: branch = "beq";break;
			case Tree.CJUMP.NE: branch = "bne";break;
			case Tree.CJUMP.LT: branch = "blt";break;
			case Tree.CJUMP.LE: branch = "ble";break;
			case Tree.CJUMP.GT: branch = "bgt";break;
			case Tree.CJUMP.GE: branch = "bge";break;
			}
			emit(new Assem.BRANCH(branch + " `s0 `s1 `j0", null, 
								L(munchExp(cjump.left), L(munchExp(cjump.right), null)), new Temp.LabelList(cjump.iftrue, null)));
			return;
		}
		if(s instanceof Tree.EXP){
			munchExp(((Tree.EXP)s).exp);
			return;
		}
	}
	void munchStm(Tree.MOVE s){
		if(s.dst instanceof Tree.TEMP){
			Temp.Temp dst = ((Tree.TEMP)s.dst).temp;
			if(s.src instanceof Tree.BINOP){
				Tree.BINOP binop = (Tree.BINOP)s.src;
				String op = "";
				Tree.Exp left = binop.left;
				Tree.Exp right = binop.right;
				switch(binop.binop){
				case Tree.BINOP.PLUS:op = "add";
					if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
						emit(new Assem.OPER("li `d0 " + (((Tree.CONST)left).value + 
														 ((Tree.CONST)right).value),
											L(dst, null), null));
						return;
					}
					if(binop.left instanceof Tree.CONST){
						emit(new Assem.OPER("addi `d0 `s0 " + ((Tree.CONST)left).value,
											L(dst, null), L(munchExp(binop.right), null)));
						return;
					}
					if(binop.right instanceof Tree.CONST){
						emit(new Assem.OPER("addi `d0 `s0 " + ((Tree.CONST)right).value,
											L(dst, null), L(munchExp(binop.left), null)));
						return;
					}
					break;
				case Tree.BINOP.MINUS:op = "sub";
					if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
						emit(new Assem.OPER("li `d0 " + (((Tree.CONST)left).value - 
														 ((Tree.CONST)right).value),
											L(dst, null), null));
						return;
					}
					if(binop.right instanceof Tree.CONST){
						emit(new Assem.OPER("addi `d0 `s0 " + (-(((Tree.CONST)right).value)),
											L(dst, null), L(munchExp(binop.left), null)));
						return;
					}
					break;
				case Tree.BINOP.MUL:op = "mul";break;
				case Tree.BINOP.DIV:op = "div";break;
				}
				emit (new Assem.OPER(op + " `d0 `s0 `s1",L(dst,null), 
									 L(munchExp(binop.left),L(munchExp(binop.right),null))));
				return;
			}
			if(s.src instanceof Tree.MEM){
				Tree.MEM mem = (Tree.MEM)s.src;
				if(mem.exp instanceof Tree.BINOP){
					Tree.BINOP binop = (Tree.BINOP)mem.exp;
					if(binop.binop == Tree.BINOP.PLUS){
						Tree.Exp left = binop.left;
						Tree.Exp right = binop.right;
						if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
							emit(new Assem.OPER("lw `d0 " + (((Tree.CONST)left).value + ((Tree.CONST)right).value) + "($zero)",
												L(dst, null), null));
							return;
						}
						if(binop.left instanceof Tree.CONST){
							emit(new Assem.OPER("lw `d0 " + ((Tree.CONST)left).value + "(`s0)", 
												L(dst, null), L(munchExp(binop.right), null)));
							return;
						}
						if(binop.right instanceof Tree.CONST){
							emit(new Assem.OPER("lw `d0 " + ((Tree.CONST)right).value + "(`s0)", 
												L(dst, null), L(munchExp(binop.left), null)));
							return;
						}
					}
					if(binop.binop == Tree.BINOP.MINUS){
						Tree.Exp left = binop.left;
						Tree.Exp right = binop.right;
						if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
							emit(new Assem.OPER("lw `d0 " + (((Tree.CONST)left).value - ((Tree.CONST)right).value) + "($zero)", 
												L(dst, null), null));
							return;
						}
						if(binop.right instanceof Tree.CONST){
							emit(new Assem.OPER("lw `d0 " + (-((Tree.CONST)right).value) + "(`s0)", 
												L(dst, null), L(munchExp(binop.left), null)));
							return;
						}
					}
				}
				if(mem.exp instanceof Tree.CONST){
					emit(new Assem.OPER("lw `d0 " + ((Tree.CONST)mem.exp).value + "($zero)", 
										L(dst,null), null));
					return;
				} 		
				emit(new Assem.OPER("lw `d0 0(`s0)", L(dst, null), L(munchExp(mem.exp), null)));
				return;
			}
			if(s.src instanceof Tree.CONST){
				emit(new Assem.OPER("li `d0 "+ ((Tree.CONST)s.src).value, L(dst,null), null));
				return;
			}
			if(s.src instanceof Tree.NAME){
				emit(new Assem.OPER("la `d0 "+ ((Tree.NAME)s.src).label.toString(), L(dst,null), null));
				return;
			}
			emit(new Assem.MOVE("move `d0 `s0",dst, munchExp(s.src)));
			return;
		}
		if(s.dst instanceof Tree.MEM){
			Tree.MEM mem = (Tree.MEM)s.dst;
			if(mem.exp instanceof Tree.BINOP){
				Tree.BINOP binop = (Tree.BINOP)mem.exp;
				Tree.Exp left = binop.left;
				Tree.Exp right = binop.right;
				if(binop.binop == Tree.BINOP.PLUS){
					if(left instanceof Tree.CONST && right instanceof Tree.CONST){
						emit(new Assem.OPER("sw `s0 " + (((Tree.CONST)left).value + ((Tree.CONST)right).value) + "($zero)", null,
											L(munchExp(s.src), null)));
						return;
					}
					if(left instanceof Tree.CONST){
						emit(new Assem.OPER("sw `s0 " + ((Tree.CONST)left).value + "(`s1)", null, 
											L(munchExp(s.src), L(munchExp(binop.right),null))));
						return;
					}
					if(right instanceof Tree.CONST){
						emit(new Assem.OPER("sw `s0 " + ((Tree.CONST)right).value + "(`s1)", null, 
											L(munchExp(s.src), L(munchExp(binop.left),null))));
						return;
					}
				}
				if(binop.binop == Tree.BINOP.MINUS){
					if(left instanceof Tree.CONST && right instanceof Tree.CONST){
						emit(new Assem.OPER("sw `s0 " + (((Tree.CONST)left).value - ((Tree.CONST)right).value) + "($zero)", null,
											L(munchExp(s.src), null)));
						return;
					}
					if(right instanceof Tree.CONST){
						emit(new Assem.OPER("sw `s0 " + (-((Tree.CONST)right).value) + "(`s1)", null, 
											L(munchExp(s.src), L(munchExp(binop.left),null))));
						return;
					}
				}
			}
			if(mem.exp instanceof Tree.CONST){
				emit(new Assem.OPER("sw `s0 " + ((Tree.CONST)mem.exp).value + "($zero)", null, 
									L(munchExp(s.src),null)));
				return;
			}
			emit(new Assem.OPER("sw `s0 0(`s1)", null, 
								L(munchExp(s.src), L(munchExp(mem.exp),null))));
		}
	}

	Temp.Temp munchExp(Tree.Exp e){
		if(e instanceof Tree.MEM)return munchExp((Tree.MEM)e);
		if(e instanceof Tree.BINOP)return munchExp((Tree.BINOP)e);
		if(e instanceof Tree.CONST){
			Temp.Temp r = new Temp.Temp();
			Tree.CONST c = (Tree.CONST)e;
			if(c.value == 0)return frame.ZERO();
			emit(new Assem.OPER("li `d0 " + c.value, L(r, null), null));
			return r;
		}
		if(e instanceof Tree.TEMP){
			return ((Tree.TEMP)e).temp;
		}
		if(e instanceof Tree.CALL){
			//just a very pseudoinstrunction
			Tree.CALL call = (Tree.CALL)e;
			//Temp.Temp r = munchExp(call.func);
			Temp.TempList l = munchArgs(0,call.args);

			emit(new Assem.CALL("jal `j0 ", frame.calldefs(), l, new Temp.LabelList(((Tree.NAME)call.func).label, null)));
			return frame.RV();
		}
		if(e instanceof Tree.NAME){
			Temp.Temp r = new Temp.Temp();
			emit(new Assem.OPER("la `d0 " + ((Tree.NAME)e).label.toString(), L(r, null), null));
			return r;
		}
		throw new Error("munchExp");

	} 

	Temp.Temp munchExp(Tree.MEM mem){
		Temp.Temp r = new Temp.Temp();
		if(mem.exp instanceof Tree.BINOP){
			Tree.BINOP binop = (Tree.BINOP)mem.exp;
			if(binop.binop == Tree.BINOP.PLUS){
				Tree.Exp left = binop.left;
				Tree.Exp right = binop.right;
				if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
					emit(new Assem.OPER("lw `d0 " + (((Tree.CONST)left).value + ((Tree.CONST)right).value) + "($zero)",
										L(r, null), null));
					return r;
				}
				if(binop.left instanceof Tree.CONST){
					emit(new Assem.OPER("lw `d0 " + ((Tree.CONST)left).value + "(`s0)", 
										L(r, null), L(munchExp(binop.right), null)));
					return r;
				}
				if(binop.right instanceof Tree.CONST){
					emit(new Assem.OPER("lw `d0 " + ((Tree.CONST)right).value + "(`s0)", 
										L(r, null), L(munchExp(binop.left), null)));
					return r;
				}
			}
			if(binop.binop == Tree.BINOP.MINUS){
				Tree.Exp left = binop.left;
				Tree.Exp right = binop.right;
				if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
					emit(new Assem.OPER("lw `d0 " + (((Tree.CONST)left).value - ((Tree.CONST)right).value) + "($zero)", 
										L(r, null), null));
					return r;
				}
				if(binop.right instanceof Tree.CONST){
					emit(new Assem.OPER("lw `d0 " + (-((Tree.CONST)right).value) + "(`s0)", 
										L(r, null), L(munchExp(binop.left), null)));
					return r;
				}
			}
		}
		if(mem.exp instanceof Tree.CONST){
			emit(new Assem.OPER("lw `d0 " + ((Tree.CONST)mem.exp).value + "($zero)", 
								L(r,null), null));
			return r;
		} 
		emit(new Assem.OPER("lw `d0 0(`s0)", L(r,null), L(munchExp(mem.exp),null)));
		return r;
	}

	Temp.TempList munchArgs(int x, Tree.ExpList args){
		if(args == null || x >= 4)return null;//only the first 4 args in register, others are in memory, so no use of reg will occur
		else return new Temp.TempList(munchExp(args.head), munchArgs(x+1, args.tail));
	}

	Temp.Temp munchExp(Tree.BINOP binop){
		Temp.Temp dst = new Temp.Temp();
		String op = "";
		Tree.Exp left = binop.left;
		Tree.Exp right = binop.right;
		switch(binop.binop){
		case Tree.BINOP.PLUS:op = "add";
			if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
				emit(new Assem.OPER("li `d0 " + (((Tree.CONST)left).value + 
												 ((Tree.CONST)right).value),
									L(dst, null), null));
				return dst;
			}
			if(binop.left instanceof Tree.CONST){
				emit(new Assem.OPER("addi `d0 `s0 " + ((Tree.CONST)left).value,
									L(dst, null), L(munchExp(binop.right), null)));
				return dst;
			}
			if(binop.right instanceof Tree.CONST){
				emit(new Assem.OPER("addi `d0 `s0 " + ((Tree.CONST)right).value,
									L(dst, null), L(munchExp(binop.left), null)));
				return dst;
			}
			break;
		case Tree.BINOP.MINUS:op = "sub";
			if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
				emit(new Assem.OPER("li `d0 " + (((Tree.CONST)left).value - 
												 ((Tree.CONST)right).value),
									L(dst, null), null));
				return dst;
			}
			if(binop.right instanceof Tree.CONST){
				emit(new Assem.OPER("addi `d0 `s0 " + (-(((Tree.CONST)right).value)),
									L(dst, null), L(munchExp(binop.left), null)));
				return dst;
			}
			break;
		case Tree.BINOP.MUL:op = "mul";break;
		case Tree.BINOP.DIV:op = "div";break;
		}
		emit (new Assem.OPER(op + " `d0 `s0 `s1",L(dst,null), 
							 L(munchExp(binop.left),L(munchExp(binop.right),null))));
		return dst;
	}

		

	Assem.InstrList codegen(Tree.Stm s){
		Assem.InstrList l;
		munchStm(s);
		l = ilist;
		ilist = last = null;
		return l;
	}
}
