package Mips;
import Temp.*;

import Util.BoolList;
import Frame.*;
/**
 *	this class is an implementation of frame
 *	you shall shall read the interface carefully and 
 *	write your implementation here
 */
public class MipsFrame implements Frame{
	private Temp.TempList L(Temp.Temp h, Temp.TempList t){
		return new Temp.TempList(h,t);
	}

	private Tree.TEMP TEMP(Temp t){
		return new Tree.TEMP(t);
	}

	public Label name;
	public AccessList formals;
	public static Temp fp = new Temp("$fp");
	public static Temp rv = new Temp("$rv");
	public static Temp sp = new Temp("$sp");
	public static Temp a0 = new Temp("$a0");
	public static Temp a1 = new Temp("$a1");
	public static Temp a2 = new Temp("$a2");
	public static Temp a3 = new Temp("$a3");
	public static Temp s0 = new Temp("$s0");
	public static Temp s1 = new Temp("$s1");
	public static Temp s2 = new Temp("$s2");
	public static Temp s3 = new Temp("$s3");
	public static Temp s4 = new Temp("$s4");
	public static Temp s5 = new Temp("$s5");
	public static Temp s6 = new Temp("$s6");
	public static Temp s7 = new Temp("$s7");
	public static TempList calleesaves = L(s0, L (s1, L(s2, L(s3, L(s4, L(s5, L(s6, L(s7, null))))))));
	public static TempList returnsink = L(zero, L(ra, L(sp, calleesaves)));

	int allocPoint = -wordSize();//local varival begin at -4;
	@Override
	public Frame newFrame( Label name , BoolList fmls ) {
		return new Mips.MipsFrame(name, fmls);
	}
	/** alloc space for formals
	 */
	public MipsFrame(Label n, BoolList fmls){
		name = n;
		int p = 0;
		formals = new AccessList(null, null);
		AccessList tempF = formals;
		if(fmls != null)do{
				tempF.tail = new AccessList(null, null);
				tempF = tempF.tail;
				if(fmls.head){
					tempF.head = new InFrame(p);
				}
				else{
					tempF.head = new InReg();
				}
				p += wordSize();
				fmls = fmls.tail;
			}while(fmls != null);
   		formals = formals.tail;
	}
	@Override
	public Access allocLocal( boolean escape ) {
		if(escape){
			Access access =  new InFrame(allocPoint);
			allocPoint -= wordSize();
			return access;
		}
		else return new InReg();
	}

	public int wordSize(){
		return 4;
	}

	@Override
	public AccessList getFormals() {
		return formals;
	}
	@Override
	public Label getName() {
		return name;
	}
	@Override
	public Tree.Exp externalCall(String s, Tree.ExpList args){
		return new Tree.CALL(new Tree.NAME(Label.label(s)), args);
	}

	@Override
	public Tree.Stm procEntryExit1(Tree.Stm body){
		int formalReg = 0;
		AccessList tAccess = formals;
		while(tAccess != null){
			if(formalReg < 4){
				Tree.MOVE move = new Tree.MOVE(tAccess.head.exp(TEMP(FP())), TEMP(FORMAL(formalReg)));
				body = new Tree.SEQ(move, body);
			}
			else {
				if(tAccess.head instanceof InReg){
					Tree.MOVE move = new Tree.MOVE(tAccess.head.exp(TEMP(FP())), 
												   new Tree.MEM(new Tree.BINOP(Tree.BINOP.PLUS, 
																			   TEMP(FP()), new Tree.CONST(wordSize() * formalReg))));
					body = new Tree.SEQ(move, body);
				}
			}
			formalReg++;
			tAccess = tAccess.tail;
		}
		return body;
	}
	@Override
	public Assem.InstrList procEntryExit2(Assem.InstrList body){
		if(a == null)return b;
		else return body.append(new InstrList(new Assem.OPER("", null, returnSink), null));
	}

	@Override
	public Temp FP() {
		return fp;
	}
	@Override
	public Temp RV() {
		return rv;
	}
	@Override
	public Temp SP(){
		return sp;
	}
	@Override
	public Temp FORMAL(int x){
		switch(x){
		case 0:return a0;
		case 1:return a1;
		case 2:return a2;
		case 3:return a3;
		}
		throw new Error("no formal big than 3");
	}
	@Override
	public TempList calldefs(){
		return null;
	}
	@Override


}
