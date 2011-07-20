package mips;
import frame.Frame;
public class Codegen {
	Frame frame;
	public Codegen(Frame f){
		frame = f;
	}
	private assem.InstrList ilist = null;
	private assem.InstrList last = null;
	private void emit(assem.Instr inst){
		if(last != null)
			last = last.tail = new assem.InstrList(inst, null);
		else 
			last = ilist = new assem.InstrList(inst, null);
	}
	private temp.TempList L(temp.AtomicTemp h, temp.TempList t){
		return new temp.TempList(h,t);
	}

	void munchStm(tree.Stm s){
		if(s instanceof tree.Seq){
			tree.Seq seq = (tree.Seq)s;
			munchStm(seq.left);
			munchStm(seq.right);
			return;
		}
		if(s instanceof tree.Move){
			munchStm((tree.Move)s);
			return;
		}
		if(s instanceof tree.Label){
			tree.Label label = (tree.Label)s;
			emit(new assem.LabelInstr(label.label.toString() + ":", label.label));
			return;
		}
		if(s instanceof tree.Jump){
			emit(new assem.JumpInstr("j `j0", ((tree.Jump)s).targets, assem.JumpInstr.J));
			return;
		}
		if(s instanceof tree.Cjump){
			tree.Cjump cjump = (tree.Cjump)s;
			String branch = "";
			int op = -1;
			switch(cjump.relop){
			case tree.Cjump.EQ: branch = "beq"; op = assem.BranchInstr.EQ; break;
			case tree.Cjump.NE: branch = "bne";op = assem.BranchInstr.NE;break;
			case tree.Cjump.LT: branch = "blt";op = assem.BranchInstr.LT;break;
			case tree.Cjump.LE: branch = "ble";op = assem.BranchInstr.LE;break;
			case tree.Cjump.GT: branch = "bgt";op = assem.BranchInstr.GT;break;
			case tree.Cjump.GE: branch = "bge";op = assem.BranchInstr.GE;break;
			}
			emit(new assem.BranchInstr(branch + " `s0 `s1 `j0", munchExp(cjump.left), munchExp(cjump.right), new temp.LabelList(cjump.iftrue, null), op));
			return;
		}
		if(s instanceof tree.ExpNoValue){
			munchExp(((tree.ExpNoValue)s).exp);
			return;
		}
	}
	void munchStm(tree.Move s){
		if(s.dst instanceof tree.Temp){
			temp.AtomicTemp dst = ((tree.Temp)s.dst).temp;
			if(s.src instanceof tree.BinOp){
				tree.BinOp binop = (tree.BinOp)s.src;
				String op = "";
				int opcode = -1;
				tree.Exp left = binop.left;
				tree.Exp right = binop.right;
				switch(binop.binop){
				case tree.BinOp.PLUS:op = "add";
					opcode = assem.BinInstr.ADD;
					if(binop.left instanceof tree.Const && binop.right instanceof tree.Const){
						emit(new assem.BinInstr("li `d0 `i",dst, null, null,
											 ((tree.Const)left).value + ((tree.Const)right).value, assem.BinInstr.LI));
						return;
					}
					if(binop.left instanceof tree.Const){
						int value = ((tree.Const)left).value;
						if(value == 0)
							emit(new assem.MoveInstr("move `d0 `s0", dst, munchExp(binop.right)));
						else  
							emit(new assem.BinInstr("addi `d0 `s0 `i" ,dst, munchExp(binop.right), null, 
												 value,  assem.BinInstr.ADDI));
						return;
					}
					if(binop.right instanceof tree.Const){
						int value = ((tree.Const)right).value;
						if(value == 0)
							emit(new assem.MoveInstr("move `d0 `s0", dst, munchExp(binop.left)));
						else  
							emit(new assem.BinInstr("addi `d0 `s0 `i" ,dst, munchExp(binop.left), null, 
												 value,  assem.BinInstr.ADDI));
						return;
					}
					break;
				case tree.BinOp.MINUS:op = "sub";
					opcode = assem.BinInstr.SUB;
					if(binop.left instanceof tree.Const && binop.right instanceof tree.Const){
						emit(new assem.BinInstr("li `d0 `i", dst, null, null,
											 ((tree.Const)left).value-((tree.Const)right).value,assem.BinInstr.LI));
						return;
					}
					if(binop.right instanceof tree.Const){
						int value = ((tree.Const)right).value;
						if(value == 0)
							emit(new assem.MoveInstr("move `d0 `s0", dst, munchExp(binop.left)));
						else  
							emit(new assem.BinInstr("addi `d0 `s0 `i" ,dst, munchExp(binop.left), null, 
												 -value,  assem.BinInstr.ADDI));
						return;
					}
					break;
				case tree.BinOp.MUL:op = "mul";opcode = assem.BinInstr.MUL;
					if(left instanceof tree.Const && right instanceof tree.Const){
						emit(new assem.BinInstr("li `d0 `i", dst, null, null,
											 ((tree.Const)left).value*((tree.Const)right).value,assem.BinInstr.LI));
						return;
					}
					if(right instanceof tree.Const){
						int t = power2(((tree.Const)right).value);
						if(t >=0){
							emit(new assem.BinInstr("sll `d0 `s0 `i" , dst, munchExp(binop.left), null,
													  t,  assem.BinInstr.SLL));
							return;
						}
					}
					if(left instanceof tree.Const){
						int t = power2(((tree.Const)left).value);
						if(t >=0){
							emit(new assem.BinInstr("sll `d0 `s0 `i" , dst, munchExp(binop.right), null,
													  t,  assem.BinInstr.SLL));
							return;
						}
					}

					break;
				case tree.BinOp.DIV:op = "div";opcode = assem.BinInstr.DIV;break;
				}
				emit (new assem.BinInstr(op + " `d0 `s0 `s1",dst, munchExp(binop.left), munchExp(binop.right), 0, opcode));
				return;
			}
			if(s.src instanceof tree.Mem){
				tree.Mem mem = (tree.Mem)s.src;
				if(mem.exp instanceof tree.BinOp){
					tree.BinOp binop = (tree.BinOp)mem.exp;
					if(binop.binop == tree.BinOp.PLUS){
						tree.Exp left = binop.left;
						tree.Exp right = binop.right;
						if(binop.left instanceof tree.Const && binop.right instanceof tree.Const){
							emit(new assem.MemInstr("lw `d0 `i(`s0)" ,dst, frame.ZERO(), null,
											   ((tree.Const)left).value + ((tree.Const)right).value, frame, assem.MemInstr.LW));
							return;
						}
						if(binop.left instanceof tree.Const){
							emit(new assem.MemInstr("lw `d0 `i(`s0)" ,dst, munchExp(binop.right), null,
												((tree.Const)left).value , frame, assem.MemInstr.LW)); 
							return;
						}
						if(binop.right instanceof tree.Const){
							emit(new assem.MemInstr("lw `d0 `i(`s0)" ,dst , munchExp(binop.left),null,
												((tree.Const)right).value, frame, assem.MemInstr.LW)); 
							return;
						}
					}
					if(binop.binop == tree.BinOp.MINUS){
						tree.Exp left = binop.left;
						tree.Exp right = binop.right;
						if(binop.left instanceof tree.Const && binop.right instanceof tree.Const){
							emit(new assem.MemInstr("lw `d0 `i(`s0)", dst, frame.ZERO(), null,
											   ((tree.Const)left).value - ((tree.Const)right).value, frame, assem.MemInstr.LW));
							return;
						}
						if(binop.right instanceof tree.Const){
							emit(new assem.MemInstr("lw `d0 `i(`s0)", dst, munchExp(binop.left), null,
												-((tree.Const)right).value, frame, assem.MemInstr.LW)); 
							return;
						}
					}
				}
				if(mem.exp instanceof tree.Const){
					emit(new assem.MemInstr("lw `d0 `i(`s0)", dst, frame.ZERO(), null,
										((tree.Const)mem.exp).value, frame, assem.MemInstr.LW)); 
					return;
				} 		
				emit(new assem.MemInstr("lw `d0 `i(`s0)",dst, munchExp(mem.exp), null,
								   0, frame, assem.MemInstr.LW));
				
				return;
			}
			if(s.src instanceof tree.Const){
				emit(new assem.BinInstr("li `d0 `i" , dst, null, null,
									 ((tree.Const)s.src).value,assem.BinInstr.LI));
				return;
			}
			if(s.src instanceof tree.Name){
				emit(new assem.BinInstr("la `d0 "+ ((tree.Name)s.src).label.toString(), dst, null, null, 0, assem.BinInstr.LA));
				return;
			}
			emit(new assem.MoveInstr("move `d0 `s0",dst, munchExp(s.src)));
			return;
		}
		if(s.dst instanceof tree.Mem){
			tree.Mem mem = (tree.Mem)s.dst;
			if(mem.exp instanceof tree.BinOp){
				tree.BinOp binop = (tree.BinOp)mem.exp;
				tree.Exp left = binop.left;
				tree.Exp right = binop.right;
				if(binop.binop == tree.BinOp.PLUS){
					if(left instanceof tree.Const && right instanceof tree.Const){
						emit(new assem.MemInstr("sw `s0 `i(`s1)", null, frame.ZERO(), munchExp(s.src),
										   ((tree.Const)left).value + ((tree.Const)right).value, frame, assem.MemInstr.SW));
						return;
					}
					if(left instanceof tree.Const){
						emit(new assem.MemInstr("sw `s0 `i(`s1)", null, munchExp(s.src), munchExp(binop.right),
										   ((tree.Const)left).value, frame, assem.MemInstr.SW));
						return;
					}
					if(right instanceof tree.Const){
						emit(new assem.MemInstr("sw `s0 `i(`s1)", null, munchExp(s.src), munchExp(binop.left),
										   ((tree.Const)right).value, frame, assem.MemInstr.SW)); 
						return;
					}
				}
				if(binop.binop == tree.BinOp.MINUS){
					if(left instanceof tree.Const && right instanceof tree.Const){
						emit(new assem.MemInstr("sw `s0 `i(`s1)", null , munchExp(s.src), frame.ZERO(), 
										   (((tree.Const)left).value - ((tree.Const)right).value) , frame, assem.MemInstr.SW));
						return;
					}
					if(right instanceof tree.Const){
						emit(new assem.MemInstr("sw `s0 `i(`s1)", null , munchExp(s.src), munchExp(binop.left),
											-((tree.Const)right).value, frame, assem.MemInstr.SW));
						return;
					}
				}
			}
			if(mem.exp instanceof tree.Const){
				emit(new assem.MemInstr("sw `s0 `i(`s1)", null, munchExp(s.src), frame.ZERO(),
									((tree.Const)mem.exp).value, frame, assem.MemInstr.SW));
				return;
			}
			emit(new assem.MemInstr("sw `s0 `i(`s1)", null,munchExp(s.src), munchExp(mem.exp),
								0, frame, assem.MemInstr.SW));
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
	temp.AtomicTemp munchExp(tree.Exp e){
		if(e instanceof tree.Mem)return munchExp((tree.Mem)e);
		if(e instanceof tree.BinOp)return munchExp((tree.BinOp)e);
		if(e instanceof tree.Const){
			temp.AtomicTemp r = new temp.AtomicTemp();
			tree.Const c = (tree.Const)e;
			if(c.value == 0)return frame.ZERO();
			emit(new assem.BinInstr("li `d0 `i" ,r, null,null, c.value, assem.BinInstr.LI));
			return r;
		}
		if(e instanceof tree.Temp){
			return ((tree.Temp)e).temp;
		}
		if(e instanceof tree.Call){
			//just a very pseudoinstrunction
			tree.Call call = (tree.Call)e;
			//Temp.Temp r = munchExp(call.func);
			temp.TempList l = munchArgs(0,call.args);

			String name = (((tree.Name)call.func).label).toString();
			String preffix = "_";
			if(name.startsWith(preffix))
				emit(new assem.CallInstr("jal `j0 ", frame.syscalldefs(), l, ((tree.Name)call.func).label));
			else
				emit(new assem.CallInstr("jal `j0 ", frame.calldefs(), l, ((tree.Name)call.func).label));
			return frame.RV();
		}
		if(e instanceof tree.Name){
			temp.AtomicTemp r = new temp.AtomicTemp();
			emit(new assem.BinInstr("la `d0 " + ((tree.Name)e).label.toString(), r, null, null, 0,  assem.BinInstr.LA));
			return r;
		}
		throw new Error("munchExp");
	} 

	temp.AtomicTemp munchExp(tree.Mem mem){
		temp.AtomicTemp dst = new temp.AtomicTemp();
		if(mem.exp instanceof tree.BinOp){
			tree.BinOp binop = (tree.BinOp)mem.exp;
			if(binop.binop == tree.BinOp.PLUS){
				tree.Exp left = binop.left;
				tree.Exp right = binop.right;
				if(binop.left instanceof tree.Const && binop.right instanceof tree.Const){
					emit(new assem.MemInstr("lw `d0 `i(`s0)" ,dst, frame.ZERO(), null,
									   ((tree.Const)left).value + ((tree.Const)right).value, frame, assem.MemInstr.LW));
					return dst;
				}
				if(binop.left instanceof tree.Const){
					emit(new assem.MemInstr("lw `d0 `i(`s0)" ,dst, munchExp(binop.right), null,
									   ((tree.Const)left).value , frame, assem.MemInstr.LW)); 
					return dst;
				}
				if(binop.right instanceof tree.Const){
					emit(new assem.MemInstr("lw `d0 `i(`s0)" ,dst , munchExp(binop.left),null,
									   ((tree.Const)right).value, frame, assem.MemInstr.LW)); 
					return dst;
				}
			}
			if(binop.binop == tree.BinOp.MINUS){
				tree.Exp left = binop.left;
				tree.Exp right = binop.right;
				if(binop.left instanceof tree.Const && binop.right instanceof tree.Const){
					emit(new assem.MemInstr("lw `d0 `i(`s0)", dst, frame.ZERO(), null,
									   ((tree.Const)left).value - ((tree.Const)right).value, frame, assem.MemInstr.LW));
					return dst;
				}
				if(binop.right instanceof tree.Const){
					emit(new assem.MemInstr("lw `d0 `i(`s0)", dst, munchExp(binop.left), null,
									   -((tree.Const)right).value, frame, assem.MemInstr.LW)); 
					return dst;
				}
			}
		}
		if(mem.exp instanceof tree.Const){
			emit(new assem.MemInstr("lw `d0 `i(`s0)", dst, frame.ZERO(), null,
							   ((tree.Const)mem.exp).value, frame, assem.MemInstr.LW)); 
			return dst;
		} 		
		emit(new assem.MemInstr("lw `d0 `i(`s0)",dst, munchExp(mem.exp), null,
						   0, frame, assem.MemInstr.LW));
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

	temp.TempList munchArgs(int x, tree.ExpList args){
		if(args == null || x >= 4)return null;//only the first 4 args in register, others are in memory, so no use of reg will occur
		else return new temp.TempList(munchExp(args.head), munchArgs(x+1, args.tail));
	}

	temp.AtomicTemp munchExp(tree.BinOp binop){
		temp.AtomicTemp dst = new temp.AtomicTemp();
		String op = "";
		int opcode = -1;
		tree.Exp left = binop.left;
		tree.Exp right = binop.right;
		switch(binop.binop){
		case tree.BinOp.PLUS:op = "add";
			opcode = assem.BinInstr.ADD;
			if(binop.left instanceof tree.Const && binop.right instanceof tree.Const){
				emit(new assem.BinInstr("li `d0 `i", dst, null, null, 
									 ((tree.Const)left).value + ((tree.Const)right).value, assem.BinInstr.LI));
				return dst;
			}
			if(binop.left instanceof tree.Const){
				int value = ((tree.Const)left).value;
				if(value == 0)
					emit(new assem.MoveInstr("move `d0 `s0", dst, munchExp(binop.right)));
				else 
					emit(new assem.BinInstr("addi `d0 `s0 `i ", dst, munchExp(binop.right), null,
									 value, assem.BinInstr.ADDI));
				return dst;
			}
			if(binop.right instanceof tree.Const){
				int value = ((tree.Const)right).value;
				if(value == 0)
					emit(new assem.MoveInstr("move `d0 `s0", dst, munchExp(binop.left)));
				else emit(new assem.BinInstr("addi `d0 `s0 `i", dst, munchExp(binop.left), null,
										  value, assem.BinInstr.ADDI));
				return dst;
			}
			break;
		case tree.BinOp.MINUS:op = "sub";
			opcode = assem.BinInstr.SUB;
			if(binop.left instanceof tree.Const && binop.right instanceof tree.Const){
				emit(new assem.BinInstr("li `d0 `i", dst, null, null, 
									 ((tree.Const)left).value - ((tree.Const)right).value, assem.BinInstr.LI));
				return dst;
			}
			if(binop.right instanceof tree.Const){
				int value = ((tree.Const)right).value;
				if(value == 0)
					emit(new assem.MoveInstr("move `d0 `s0", dst, munchExp(binop.left)));
				else emit(new assem.BinInstr("addi `d0 `s0 `i", dst, munchExp(binop.left), null,
									 -value, assem.BinInstr.ADDI));
				return dst;
			}
			break;
		case tree.BinOp.MUL:op = "mul";
			opcode = assem.BinInstr.MUL;
			if(left instanceof tree.Const && right instanceof tree.Const){
				emit(new assem.BinInstr("li `d0 `i", dst, null, null,
									 ((tree.Const)left).value*((tree.Const)right).value,assem.BinInstr.LI));
				return dst;
			}
			if(right instanceof tree.Const){
				int t = power2(((tree.Const)right).value);
				if(t >=0){
					emit(new assem.BinInstr("sll `d0 `s0 `i" , dst, munchExp(binop.left), null,
											  t,  assem.BinInstr.SLL));
					return dst;
				}
			}
			if(left instanceof tree.Const){
				int t = power2(((tree.Const)left).value);
				if(t >=0){
					emit(new assem.BinInstr("sll `d0 `s0 `i" , dst, munchExp(binop.right), null,
											  t,  assem.BinInstr.SLL));
					return dst;
				}
			}

			break;
		case tree.BinOp.DIV:op = "div";opcode = assem.BinInstr.DIV;break;
		}
		emit (new assem.BinInstr(op + " `d0 `s0 `s1",dst, munchExp(binop.left), munchExp(binop.right),
							  0,opcode));
		return dst;
	}

		

	assem.InstrList codegen(tree.Stm s){
		assem.InstrList l;
		munchStm(s);
		l = ilist;
		ilist = last = null;
		return l;
	}
}
