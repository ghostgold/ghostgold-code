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
	
	public Temp getReg(int x){
		switch(x){
		case 0:return zero;
		case 2:return v0;
		case 3:return v1;
		case 4:return a0;
		case 5:return a1;
		case 6:return a2;
		case 7:return a3;
		case 8:return t0;
		case 9:return t1;
		case 10:return t2;
		case 11:return t3;
		case 12:return t4;
		case 13:return t5;
		case 14:return t6;
		case 15:return t7;
		case 16:return s0;
		case 17:return s1;
		case 18:return s2;
		case 19:return s3;
		case 20:return s4;
		case 21:return s5;
		case 22:return s6;
		case 23:return s7;
		case 24:return t8;
		case 25:return t9;
		case 29:return sp;
		case 30:return fp;
		case 31:return ra;
		}
		throw new Error("no Reg");
	}
	public Label name;
	public AccessList formals;
	public Temp ffp = new Temp();
	public static Temp zero = new Temp("$zero", 0);
	public static Temp fp = new Temp("$fp", 30);
	public static Temp sp = new Temp("$sp", 29);
	public static Temp v0 = new Temp("$v0", 2);
	public static Temp v1 = new Temp("$v1",3);
	public static Temp ra = new Temp("$ra",31);
	public static Temp a0 = new Temp("$a0",4);
	public static Temp a1 = new Temp("$a1",5);
	public static Temp a2 = new Temp("$a2",6);
	public static Temp a3 = new Temp("$a3",7);
	public static Temp s0 = new Temp("$s0",16);
	public static Temp s1 = new Temp("$s1",17);
	public static Temp s2 = new Temp("$s2",18);
	public static Temp s3 = new Temp("$s3",19);
	public static Temp s4 = new Temp("$s4",20);
	public static Temp s5 = new Temp("$s5",21);
	public static Temp s6 = new Temp("$s6",22);
	public static Temp s7 = new Temp("$s7",23);
	public static Temp t0 = new Temp("$t0",8);
	public static Temp t1 = new Temp("$t1",9);
	public static Temp t2 = new Temp("$t2",10);
	public static Temp t3 = new Temp("$t3",11);
	public static Temp t4 = new Temp("$t4",12);
	public static Temp t5 = new Temp("$t5",13);
	public static Temp t6 = new Temp("$t6",14);
	public static Temp t7 = new Temp("$t7",15);
	public static Temp t8 = new Temp("$t8",24);
	public static Temp t9 = new Temp("$t9",25);
	public static Temp k0 = new Temp("$k0",26);
	public static Temp k1 = new Temp("$k1",27);
	public static Temp gp = new Temp("$gp",28);
	public static TempList parameterReg = L(a0, L(a1, L(a2, L(a3,null))));
	public static TempList calleesaves = L(s0, L (s1, L(s2, L(s3, L(s4, L(s5, L(s6, L(s7, L(ra,null)))))))));
	public static TempList callersaves = L(t0, L(t1, L (t2, L(t3, L(t4, L(t5, L(t6, L(t7, L(t8, L(t9,null))))))))));
	public static TempList returnsink = L(zero,L(ra, L(sp, calleesaves)));

	int allocPoint = -2*wordSize();//local varival begin at -4;
	int outGoing = 0;
	@Override

	public Frame newFrame( Label name , BoolList fmls , int out) {
		return new Mips.MipsFrame(name, fmls, out);
	}
	/** alloc space for formals
	 */
	public MipsFrame(Label n, BoolList fmls, int out){
		outGoing = out;
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
		return new Tree.CALL(new Tree.NAME(Label.label("_"+s, false)), args);
	}

	@Override
	public Tree.Stm procEntryExit1(Tree.Stm body){
		int formalReg = 0;
		int argNum =0;
		AccessList tAccess = formals;
		if(body == null)body = new Tree.EXP(new Tree.CONST(0));
		while (tAccess != null){
			if(!tAccess.head.escape()){
				Tree.MOVE move;
				if(formalReg < 4){
					move = new Tree.MOVE(tAccess.head.exp(TEMP(FP())), TEMP(FORMAL(formalReg)));
					formalReg++;
				}
				else {
					 move = new Tree.MOVE(tAccess.head.exp(TEMP(FP())), 
												   new Tree.MEM(new Tree.BINOP(Tree.BINOP.PLUS, 
																			   TEMP(FP()), new Tree.CONST(wordSize() * argNum))));
				}
				body = new Tree.SEQ(move, body);
			}
			tAccess = tAccess.tail;
			argNum++;
		}
		body = new Tree.SEQ(new Tree.MOVE(TEMP(FFP()),new Tree.MEM(TEMP(FP()))), body);
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
		if(body == null)return new Assem.InstrList(new Assem.CALL("", null, returnsink, null), null);
		else body.append(new Assem.InstrList(new Assem.CALL("", null, returnsink, null), null));
		return body;
	}
	@Override
	public Assem.InstrList procEntryExit3(Assem.InstrList body){
		if(body == null)return null;
		//		Temp savefp = new Temp();
		body = new Assem.InstrList(new Assem.MEM("sw `s0 `i(`s1)", null, fp,sp, -4, this, Assem.MEM.SW),
								   new Assem.InstrList(new Assem.MOVE("move `d0 `s0", fp, sp), 
													   new Assem.InstrList(new Assem.BINOP("addi `d0 `s0 `i", sp, sp, null, -(this.framesize()), Assem.BINOP.ADDI), body)));
		body = new Assem.InstrList(new Assem.LABEL(name.toString()+":", name), body);
		body.append(new Assem.InstrList(new Assem.MOVE("move `d0 `s0", sp, fp), 
										new Assem.InstrList(new Assem.MEM("lw `d0 `i(`s0)", fp, fp, null, -4, this, Assem.MEM.SW), null)));
		body.append(new Assem.InstrList(new Assem.CALL("jr `s0", null, L(ra, null), null), null));
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
	public Temp FFP(){
		return ffp;
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
		return L(v0,L(v1,L(a0,L(a1,L(a2,L(a3,L(t0,L(t1,L(t2,L(t3,L(t4,L(t5,L(t6,L(t7,L(t8,L(t9,L(ra,null)))))))))))))))));

	}
	@Override
	public TempList syscalldefs(){
		return L(v0,L(a0,L(a1,L(a2,L(a3,L(ra,null))))));
	}
	@Override
	public TempList calleeSaves(){
		return calleesaves;
	}
	@Override
	public TempList parameters(){
		return parameterReg;
	}
	@Override
	public Temp ZERO(){
		return zero;
	}

	@Override
	public TempList registers(){
		return L(zero, L(v0,L(v1,L(a0,L(a1,L(a2,L(a3,L(t0,L(t1,L(t2,L(t3,L(t4,L(t5,L(t6,L(t7,L(t8,L(t9,L(s0,L(s1,L(s2,L(s3,L(s4,L(s5,L(s6,L(s7,L(sp,L(fp,L(ra, null))))))))))))))))))))))))))));
	}
	public int framesize(){
		return -allocPoint + wordSize()*outGoing + 4;
	}
}
