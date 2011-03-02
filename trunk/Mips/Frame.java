package Mips;
import temp.Temp;
import temp.Lable;
import util.BoolList;
import frame.*;
/**
 *	this class is an implementation of frame
 *	you shall shall read the interface carefully and 
 *	write your implementation here
 */
public class Frame implements Frame.Frame{

	public Label name;
	public AccessList formals;
	public static Temp fp;
	int allocPoint;

	@Override
	public Frame.Frame newFrame( temp.Label name , BoolList fmls ) {
		return new Mips.Frame(name, fmls);
	}
	
	public Frame(temp.Label n, BoolList fmls){
		name = n;
		int p = wordSize();
		formals = new AccessList(null, null);
		AccessList tempF = formals;
		if(fmls != null)do{
				tempF.tail = new AccessList(null, null);
				tempF = tempF.tail;
				Access head;
				if(fmls.head){
					head = new InFrame(p);
					p += wordSize();
				}
				else{
					head = new InReg();
				}
				tempF.head = head;
				fmls = fmls.tail;
			}while(fmls != null)
	}
	@Override
	public Temp FP() {
		return fp;
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
	public temp.Label getName() {
		return name;
	}
	@Override
	public Tree.Exp exernalCall(String s, Tree.ExpList args){
		return newn Tree.Call(new Tree.NAME(Temp.label(s)), args);
	}
}
