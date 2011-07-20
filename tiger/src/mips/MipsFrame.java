package mips;
import temp.*;
import util.BoolList;
import frame.*;

/**
 *	this class is an implementation of frame
 *	you shall shall read the interface carefully and 
 *	write your implementation here
 */
public class MipsFrame implements Frame{
	private static TempList L(AtomicTemp h, TempList t){
		return new TempList(h,t);
	}

	private static tree.Temp TEMP(AtomicTemp t){
		return new tree.Temp(t);
	}
	
	public AtomicTemp getReg(int x){
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
	public AtomicLabel name;
	public AccessList formals;
	public AtomicTemp ffp = new AtomicTemp();
	public static AtomicTemp zero = new AtomicTemp("$zero", 0);
	public static AtomicTemp fp = new AtomicTemp("$fp", 30);
	public static AtomicTemp sp = new AtomicTemp("$sp", 29);
	public static AtomicTemp v0 = new AtomicTemp("$v0", 2);
	public static AtomicTemp v1 = new AtomicTemp("$v1",3);
	public static AtomicTemp ra = new AtomicTemp("$ra",31);
	public static AtomicTemp a0 = new AtomicTemp("$a0",4);
	public static AtomicTemp a1 = new AtomicTemp("$a1",5);
	public static AtomicTemp a2 = new AtomicTemp("$a2",6);
	public static AtomicTemp a3 = new AtomicTemp("$a3",7);
	public static AtomicTemp s0 = new AtomicTemp("$s0",16);
	public static AtomicTemp s1 = new AtomicTemp("$s1",17);
	public static AtomicTemp s2 = new AtomicTemp("$s2",18);
	public static AtomicTemp s3 = new AtomicTemp("$s3",19);
	public static AtomicTemp s4 = new AtomicTemp("$s4",20);
	public static AtomicTemp s5 = new AtomicTemp("$s5",21);
	public static AtomicTemp s6 = new AtomicTemp("$s6",22);
	public static AtomicTemp s7 = new AtomicTemp("$s7",23);
	public static AtomicTemp t0 = new AtomicTemp("$t0",8);
	public static AtomicTemp t1 = new AtomicTemp("$t1",9);
	public static AtomicTemp t2 = new AtomicTemp("$t2",10);
	public static AtomicTemp t3 = new AtomicTemp("$t3",11);
	public static AtomicTemp t4 = new AtomicTemp("$t4",12);
	public static AtomicTemp t5 = new AtomicTemp("$t5",13);
	public static AtomicTemp t6 = new AtomicTemp("$t6",14);
	public static AtomicTemp t7 = new AtomicTemp("$t7",15);
	public static AtomicTemp t8 = new AtomicTemp("$t8",24);
	public static AtomicTemp t9 = new AtomicTemp("$t9",25);
	public static AtomicTemp k0 = new AtomicTemp("$k0",26);
	public static AtomicTemp k1 = new AtomicTemp("$k1",27);
	public static AtomicTemp gp = new AtomicTemp("$gp",28);
	public static TempList parameterReg = L(a0, L(a1, L(a2, L(a3,null))));
	public static TempList calleesaves = L(s0, L (s1, L(s2, L(s3, L(s4, L(s5, L(s6, L(s7, L(ra,null)))))))));
	public static TempList callersaves = L(t0, L(t1, L (t2, L(t3, L(t4, L(t5, L(t6, L(t7, L(t8, L(t9,null))))))))));
	public static TempList returnsink = L(v0,L(zero,L(ra, L(sp, calleesaves))));

	int allocPoint = -2*wordSize();//local varival begin at -4;
	int outGoing = 0;
	@Override

	public Frame newFrame( AtomicLabel name , BoolList fmls , int out) {
		return new mips.MipsFrame(name, fmls, out);
	}
	/** alloc space for formals
	 */
	public MipsFrame(AtomicLabel n, BoolList fmls, int out){
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
	public AtomicLabel getName() {
		return name;
	}
	@Override
	public tree.Exp externalCall(String s, tree.ExpList args){
		return new tree.Call(new tree.Name(AtomicLabel.label("_"+s, false)), args);
	}

	@Override
	public tree.Stm procEntryExit1(tree.Stm body){
		int formalReg = 0;
		int argNum =0;
		AccessList tAccess = formals;
		if(body == null)body = new tree.ExpNoValue(new tree.Const(0));
		while (tAccess != null){
			if(!tAccess.head.escape()){
				tree.Move move;
				if(formalReg < 4){
					move = new tree.Move(tAccess.head.exp(TEMP(FP())), TEMP(FORMAL(formalReg)));
					formalReg++;
				}
				else {
					 move = new tree.Move(tAccess.head.exp(TEMP(FP())), 
												   new tree.Mem(new tree.BinOp(tree.BinOp.PLUS, 
																			   TEMP(FP()), new tree.Const(wordSize() * argNum))));
				}
				body = new tree.Seq(move, body);
			}
			tAccess = tAccess.tail;
			argNum++;
		}
		body = new tree.Seq(new tree.Move(TEMP(FFP()),new tree.Mem(TEMP(FP()))), body);
		TempList savereg = calleeSaves();
		while(savereg != null){
			AtomicTemp save = new AtomicTemp();
			body = new tree.Seq(new tree.Move(TEMP(save), TEMP(savereg.head)),body);
			body = new tree.Seq(body, new tree.Move(TEMP(savereg.head), TEMP(save)));
			savereg = savereg.tail;
		}
		return body;
	}
	@Override
	public assem.InstrList procEntryExit2(assem.InstrList body){
		if(body == null)return new assem.InstrList(new assem.CallInstr("", null, returnsink, null), null);
		else body.append(new assem.InstrList(new assem.CallInstr("", null, returnsink, null), null));
		return body;
	}
	@Override
	public assem.InstrList procEntryExit3(assem.InstrList body){
		if(body == null)return null;
		//		Temp savefp = new Temp();
		body = new assem.InstrList(new assem.MemInstr("sw `s0 `i(`s1)", null, fp,sp, -4, this, assem.MemInstr.SW),
								   new assem.InstrList(new assem.MoveInstr("move `d0 `s0", fp, sp), 
													   new assem.InstrList(new assem.BinInstr("addi `d0 `s0 `i", sp, sp, null, -(this.framesize()), assem.BinInstr.ADDI), body)));
		body = new assem.InstrList(new assem.LabelInstr(name.toString()+":", name), body);
		body.append(new assem.InstrList(new assem.MoveInstr("move `d0 `s0", sp, fp), 
										new assem.InstrList(new assem.MemInstr("lw `d0 `i(`s0)", fp, fp, null, -4, this, assem.MemInstr.SW), null)));
		body.append(new assem.InstrList(new assem.CallInstr("jr `s0", null, L(ra, null), null), null));
		return body;
	}
	@Override
	public assem.InstrList codegen(tree.Stm stm){
		return (new Codegen(this)).codegen(stm);
	}
	@Override
	public AtomicTemp FP() {
		return fp;
	}
	public AtomicTemp FFP(){
		return ffp;
	}
	@Override
	public AtomicTemp RV() {

		return v0;
	}
	@Override
	public AtomicTemp SP(){
		return sp;
	}
	@Override
	public AtomicTemp FORMAL(int x){
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
	public AtomicTemp ZERO(){
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
