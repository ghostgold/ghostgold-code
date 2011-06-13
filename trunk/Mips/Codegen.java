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
			emit(new Assem.JUMP("j `j0", ((Tree.JUMP)s).targets, Assem.JUMP.J));
			return;
		}
		if(s instanceof Tree.CJUMP){
			Tree.CJUMP cjump = (Tree.CJUMP)s;
			String branch = "";
			int op = -1;
			switch(cjump.relop){
			case Tree.CJUMP.EQ: branch = "beq"; op = Assem.BRANCH.EQ; break;
			case Tree.CJUMP.NE: branch = "bne";op = Assem.BRANCH.NE;break;
			case Tree.CJUMP.LT: branch = "blt";op = Assem.BRANCH.LT;break;
			case Tree.CJUMP.LE: branch = "ble";op = Assem.BRANCH.LE;break;
			case Tree.CJUMP.GT: branch = "bgt";op = Assem.BRANCH.GT;break;
			case Tree.CJUMP.GE: branch = "bge";op = Assem.BRANCH.GE;break;
			}
			emit(new Assem.BRANCH(branch + " `s0 `s1 `j0", munchExp(cjump.left), munchExp(cjump.right), new Temp.LabelList(cjump.iftrue, null), op));
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
				int opcode = -1;
				Tree.Exp left = binop.left;
				Tree.Exp right = binop.right;
				switch(binop.binop){
				case Tree.BINOP.PLUS:op = "add";
					opcode = Assem.BINOP.ADD;
					if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
						emit(new Assem.BINOP("li `d0 `i",dst, null, null,
											 ((Tree.CONST)left).value + ((Tree.CONST)right).value, Assem.BINOP.LI));
						return;
					}
					if(binop.left instanceof Tree.CONST){
						emit(new Assem.BINOP("addi `d0 `s0 `i" ,dst, munchExp(binop.right), null, 
											 ((Tree.CONST)left).value,  Assem.BINOP.ADDI));
						return;
					}
					if(binop.right instanceof Tree.CONST){
						emit(new Assem.BINOP("addi `d0 `s0 `i" ,dst, munchExp(binop.left), null, 
											 ((Tree.CONST)right).value,  Assem.BINOP.ADDI));
						return;
					}
					break;
				case Tree.BINOP.MINUS:op = "sub";
					opcode = Assem.BINOP.SUB;
					if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
						emit(new Assem.BINOP("li `d0 `i", dst, null, null,
											 ((Tree.CONST)left).value-((Tree.CONST)right).value,Assem.BINOP.LI));
						return;
					}
					if(binop.right instanceof Tree.CONST){
						emit(new Assem.BINOP("addi `d0 `s0 `i" , dst, munchExp(binop.left), null,
											-(((Tree.CONST)right).value),  Assem.BINOP.ADDI));
						return;
					}
					break;
				case Tree.BINOP.MUL:op = "mul";opcode = Assem.BINOP.MUL;
					if(left instanceof Tree.CONST && right instanceof Tree.CONST){
						emit(new Assem.BINOP("li `d0 `i", dst, null, null,
											 ((Tree.CONST)left).value*((Tree.CONST)right).value,Assem.BINOP.LI));
						return;
					}
					if(right instanceof Tree.CONST){
						int t = power2(((Tree.CONST)right).value);
						if(t >=0)emit(new Assem.BINOP("sll `d0 `s0 `i" , dst, munchExp(binop.left), null,
													  t,  Assem.BINOP.SLL));
						return;
					}
					if(left instanceof Tree.CONST){
						int t = power2(((Tree.CONST)left).value);
						if(t >=0)emit(new Assem.BINOP("sll `d0 `s0 `i" , dst, munchExp(binop.right), null,
													  t,  Assem.BINOP.SLL));
						return;
					}

					break;
				case Tree.BINOP.DIV:op = "div";opcode = Assem.BINOP.DIV;break;
				}
				emit (new Assem.BINOP(op + " `d0 `s0 `s1",dst, munchExp(binop.left), munchExp(binop.right), 0, opcode));
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
							emit(new Assem.MEM("lw `d0 `i(`s0)" ,dst, frame.ZERO(), null,
											   ((Tree.CONST)left).value + ((Tree.CONST)right).value, frame, Assem.MEM.LW));
							return;
						}
						if(binop.left instanceof Tree.CONST){
							emit(new Assem.MEM("lw `d0 `i(`s0)" ,dst, munchExp(binop.right), null,
												((Tree.CONST)left).value , frame, Assem.MEM.LW)); 
							return;
						}
						if(binop.right instanceof Tree.CONST){
							emit(new Assem.MEM("lw `d0 `i(`s0)" ,dst , munchExp(binop.left),null,
												((Tree.CONST)right).value, frame, Assem.MEM.LW)); 
							return;
						}
					}
					if(binop.binop == Tree.BINOP.MINUS){
						Tree.Exp left = binop.left;
						Tree.Exp right = binop.right;
						if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
							emit(new Assem.MEM("lw `d0 `i(`s0)", dst, frame.ZERO(), null,
											   ((Tree.CONST)left).value - ((Tree.CONST)right).value, frame, Assem.MEM.LW));
							return;
						}
						if(binop.right instanceof Tree.CONST){
							emit(new Assem.MEM("lw `d0 `i(`s0)", dst, munchExp(binop.left), null,
												-((Tree.CONST)right).value, frame, Assem.MEM.LW)); 
							return;
						}
					}
				}
				if(mem.exp instanceof Tree.CONST){
					emit(new Assem.MEM("lw `d0 `i(`s0)", dst, frame.ZERO(), null,
										((Tree.CONST)mem.exp).value, frame, Assem.MEM.LW)); 
					return;
				} 		
				emit(new Assem.MEM("lw `d0 `i(`s0)",dst, munchExp(mem.exp), null,
								   0, frame, Assem.MEM.LW));
				
				return;
			}
			if(s.src instanceof Tree.CONST){
				emit(new Assem.BINOP("li `d0 `i" , dst, null, null,
									 ((Tree.CONST)s.src).value,Assem.BINOP.LI));
				return;
			}
			if(s.src instanceof Tree.NAME){
				emit(new Assem.BINOP("la `d0 "+ ((Tree.NAME)s.src).label.toString(), dst, null, null, 0, Assem.BINOP.LA));
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
						emit(new Assem.MEM("sw `s0 `i(`s1)", null, frame.ZERO(), munchExp(s.src),
										   ((Tree.CONST)left).value + ((Tree.CONST)right).value, frame, Assem.MEM.SW));
						return;
					}
					if(left instanceof Tree.CONST){
						emit(new Assem.MEM("sw `s0 `i(`s1)", null, munchExp(s.src), munchExp(binop.right),
										   ((Tree.CONST)left).value, frame, Assem.MEM.SW));
						return;
					}
					if(right instanceof Tree.CONST){
						emit(new Assem.MEM("sw `s0 `i(`s1)", null, munchExp(s.src), munchExp(binop.left),
										   ((Tree.CONST)right).value, frame, Assem.MEM.SW)); 
						return;
					}
				}
				if(binop.binop == Tree.BINOP.MINUS){
					if(left instanceof Tree.CONST && right instanceof Tree.CONST){
						emit(new Assem.MEM("sw `s0 `i(`s1)", null , munchExp(s.src), frame.ZERO(), 
										   (((Tree.CONST)left).value - ((Tree.CONST)right).value) , frame, Assem.MEM.SW));
						return;
					}
					if(right instanceof Tree.CONST){
						emit(new Assem.MEM("sw `s0 `i(`s1)", null , munchExp(s.src), munchExp(binop.left),
											-((Tree.CONST)right).value, frame, Assem.MEM.SW));
						return;
					}
				}
			}
			if(mem.exp instanceof Tree.CONST){
				emit(new Assem.MEM("sw `s0 `i(`s1)", null, munchExp(s.src), frame.ZERO(),
									((Tree.CONST)mem.exp).value, frame, Assem.MEM.SW));
				return;
			}
			emit(new Assem.MEM("sw `s0 `i(`s1)", null,munchExp(s.src), munchExp(mem.exp),
								0, frame, Assem.MEM.SW));
		}
	}
	int power2(int x){
		if(x == 1)return 0;
		if(x == 2)return 1;
		if(x == 4)return 2;
		if(x == 8)return 3;
		if(x == 16)return 4;
		if(x == 32)return 5;
		if(x == 64)return 6;
		if(x == 128)return 7;
		if(x == 256)return 8;
		if(x == 512)return 9;
		if(x == 1024)return 10;
		return -1;
	}
	Temp.Temp munchExp(Tree.Exp e){
		if(e instanceof Tree.MEM)return munchExp((Tree.MEM)e);
		if(e instanceof Tree.BINOP)return munchExp((Tree.BINOP)e);
		if(e instanceof Tree.CONST){
			Temp.Temp r = new Temp.Temp();
			Tree.CONST c = (Tree.CONST)e;
			if(c.value == 0)return frame.ZERO();
			emit(new Assem.BINOP("li `d0 `i" ,r, null,null, c.value, Assem.BINOP.LI));
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
			emit(new Assem.BINOP("la `d0 " + ((Tree.NAME)e).label.toString(), r, null, null, 0,  Assem.BINOP.LA));
			return r;
		}
		throw new Error("munchExp");
	} 

	Temp.Temp munchExp(Tree.MEM mem){
		Temp.Temp dst = new Temp.Temp();
		if(mem.exp instanceof Tree.BINOP){
			Tree.BINOP binop = (Tree.BINOP)mem.exp;
			if(binop.binop == Tree.BINOP.PLUS){
				Tree.Exp left = binop.left;
				Tree.Exp right = binop.right;
				if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
					emit(new Assem.MEM("lw `d0 `i(`s0)" ,dst, frame.ZERO(), null,
									   ((Tree.CONST)left).value + ((Tree.CONST)right).value, frame, Assem.MEM.LW));
					return dst;
				}
				if(binop.left instanceof Tree.CONST){
					emit(new Assem.MEM("lw `d0 `i(`s0)" ,dst, munchExp(binop.right), null,
									   ((Tree.CONST)left).value , frame, Assem.MEM.LW)); 
					return dst;
				}
				if(binop.right instanceof Tree.CONST){
					emit(new Assem.MEM("lw `d0 `i(`s0)" ,dst , munchExp(binop.left),null,
									   ((Tree.CONST)right).value, frame, Assem.MEM.LW)); 
					return dst;
				}
			}
			if(binop.binop == Tree.BINOP.MINUS){
				Tree.Exp left = binop.left;
				Tree.Exp right = binop.right;
				if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
					emit(new Assem.MEM("lw `d0 `i(`s0)", dst, frame.ZERO(), null,
									   ((Tree.CONST)left).value - ((Tree.CONST)right).value, frame, Assem.MEM.LW));
					return dst;
				}
				if(binop.right instanceof Tree.CONST){
					emit(new Assem.MEM("lw `d0 `i(`s0)", dst, munchExp(binop.left), null,
									   -((Tree.CONST)right).value, frame, Assem.MEM.LW)); 
					return dst;
				}
			}
		}
		if(mem.exp instanceof Tree.CONST){
			emit(new Assem.MEM("lw `d0 `i(`s0)", dst, frame.ZERO(), null,
							   ((Tree.CONST)mem.exp).value, frame, Assem.MEM.LW)); 
			return dst;
		} 		
		emit(new Assem.MEM("lw `d0 `i(`s0)",dst, munchExp(mem.exp), null,
						   0, frame, Assem.MEM.LW));
		return dst;


		/*		if(mem.exp instanceof Tree.BINOP){

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
		return r;*/
	}

	Temp.TempList munchArgs(int x, Tree.ExpList args){
		if(args == null || x >= 4)return null;//only the first 4 args in register, others are in memory, so no use of reg will occur
		else return new Temp.TempList(munchExp(args.head), munchArgs(x+1, args.tail));
	}

	Temp.Temp munchExp(Tree.BINOP binop){
		Temp.Temp dst = new Temp.Temp();
		String op = "";
		int opcode = -1;
		Tree.Exp left = binop.left;
		Tree.Exp right = binop.right;
		switch(binop.binop){
		case Tree.BINOP.PLUS:op = "add";
			opcode = Assem.BINOP.ADD;
			if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
				emit(new Assem.BINOP("li `d0 `i", dst, null, null, 
									 ((Tree.CONST)left).value + ((Tree.CONST)right).value, Assem.BINOP.LI));
				return dst;
			}
			if(binop.left instanceof Tree.CONST){
				emit(new Assem.BINOP("addi `d0 `s0 `i ", dst, munchExp(binop.right), null,
									 ((Tree.CONST)left).value, Assem.BINOP.ADDI));
				return dst;
			}
			if(binop.right instanceof Tree.CONST){
				emit(new Assem.BINOP("addi `d0 `s0 `i", dst, munchExp(binop.left), null,
									 ((Tree.CONST)right).value, Assem.BINOP.ADDI));
				return dst;
			}
			break;
		case Tree.BINOP.MINUS:op = "sub";
			opcode = Assem.BINOP.SUB;
			if(binop.left instanceof Tree.CONST && binop.right instanceof Tree.CONST){
				emit(new Assem.BINOP("li `d0 `i", dst, null, null, 
									 ((Tree.CONST)left).value - ((Tree.CONST)right).value, Assem.BINOP.LI));
				return dst;
			}
			if(binop.right instanceof Tree.CONST){
				emit(new Assem.BINOP("addi `d0 `s0 `i", dst, munchExp(binop.left), null,
									 -((Tree.CONST)right).value, Assem.BINOP.ADDI));
				return dst;
			}
			break;
		case Tree.BINOP.MUL:op = "mul";
			opcode = Assem.BINOP.MUL;
			if(left instanceof Tree.CONST && right instanceof Tree.CONST){
				emit(new Assem.BINOP("li `d0 `i", dst, null, null,
									 ((Tree.CONST)left).value*((Tree.CONST)right).value,Assem.BINOP.LI));
				return dst;
			}
			if(right instanceof Tree.CONST){
				int t = power2(((Tree.CONST)right).value);
				if(t >=0)emit(new Assem.BINOP("sll `d0 `s0 `i" , dst, munchExp(binop.left), null,
											  t,  Assem.BINOP.SLL));
				return dst;
			}
			if(left instanceof Tree.CONST){
				int t = power2(((Tree.CONST)left).value);
				if(t >=0)emit(new Assem.BINOP("sll `d0 `s0 `i" , dst, munchExp(binop.right), null,
											  t,  Assem.BINOP.SLL));
				return dst;
			}

			break;
		case Tree.BINOP.DIV:op = "div";opcode = Assem.BINOP.DIV;break;
		}
		emit (new Assem.BINOP(op + " `d0 `s0 `s1",dst, munchExp(binop.left), munchExp(binop.right),
							  0,opcode));
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
