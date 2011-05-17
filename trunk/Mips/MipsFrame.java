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
	private static TempList L(Temp h, TempList t){
		return new TempList(h,t);
	}

	private static Tree.TEMP TEMP(Temp t){
		return new Tree.TEMP(t);
	}

	public Label name;
	public AccessList formals;
	public static Temp zero = new Temp("$zero");
	public static Temp fp = new Temp("$fp");
	public static Temp sp = new Temp("$sp");
	public static Temp v0 = new Temp("$v0");
	public static Temp v1 = new Temp("$v1");
	public static Temp ra = new Temp("$ra");
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
	public static Temp t0 = new Temp("$t0");
	public static Temp t1 = new Temp("$t1");
	public static Temp t2 = new Temp("$t2");
	public static Temp t3 = new Temp("$t3");
	public static Temp t4 = new Temp("$t4");
	public static Temp t5 = new Temp("$t5");
	public static Temp t6 = new Temp("$t6");
	public static Temp t7 = new Temp("$t7");
	public static Temp t8 = new Temp("$t8");
	public static Temp t9 = new Temp("$t9");
	public static Temp k0 = new Temp("$k0");
	public static Temp k1 = new Temp("$k1");
	public static Temp gp = new Temp("$gp");

	public static TempList calleesaves = L(s0, L (s1, L(s2, L(s3, L(s4, L(s5, L(s6, L(s7, null))))))));
	public static TempList callersaves = L(t0, L(t1, L (t2, L(t3, L(t4, L(t5, L(t6, L(t7, L(t8, L(t9,null))))))))));
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
		TempList savereg = calleeSaves();
		while(savereg != null){
			Temp save = new Temp();
			body = new Tree.SEQ(new Tree.MOVE(TEMP(save), TEMP(savereg.head)),body);
			body = new Tree.SEQ(body, new Tree.MOVE(TEMP(savereg.head), TEMP(save)));
			savereg = savereg.tail;
		}
		return body;
	}
	@Override
	public Assem.InstrList procEntryExit2(Assem.InstrList body){
		if(body == null)return new Assem.InstrList(new Assem.OPER("", null, returnsink), null);
		else body.append(new Assem.InstrList(new Assem.OPER("", null, returnsink), null));
		return body;
	}
	@Override
	public Assem.InstrList codegen(Tree.Stm stm){
		return (new Codegen(this)).codegen(stm);
	}
	@Override
	public Temp FP() {
		return fp;
	}
	@Override
	public Temp RV() {
		return v0;
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
	public TempList calleeSaves(){
		return calleesaves;
	}
	@Override
	public Temp ZERO(){
		return zero;
	}
	@Override
	public TempList registers(){
		//todo
		return null;
	}
}
