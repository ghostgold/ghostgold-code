package Mips;
import Temp.Temp;
import Temp.Label;
import Util.BoolList;
import Frame.*;
/**
 *	this class is an implementation of frame
 *	you shall shall read the interface carefully and 
 *	write your implementation here
 */
public class MipsFrame implements Frame{

	public Label name;
	public AccessList formals;
	public static Temp fp = new Temp("fp");
	public static Temp rv = new Temp("rv");
	int allocPoint;
	int p;
	@Override
	public Frame newFrame( Label name , BoolList fmls ) {
		return new Mips.MipsFrame(name, fmls);
	}
	
	public MipsFrame(Label n, BoolList fmls){
		name = n;
		p = wordSize();
		formals = new AccessList(null, null);
		AccessList tempF = formals;
		if(fmls != null)do{
				tempF.tail = new AccessList(null, null);
				tempF = tempF.tail;
				if(fmls.head){
					tempF.head = new InFrame(p);
					p += wordSize();
				}
				else{
					tempF.head = new InReg();
				}
				fmls = fmls.tail;
			}while(fmls != null);
   		formals = formals.tail;
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
}
