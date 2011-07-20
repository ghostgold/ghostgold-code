package assem;
import temp.*;
public class BranchInstr extends Oper
{
	public final static int EQ = 10, NE = 11, LT = 12, LE = 13, GT = 14, GE = 15;


	public BranchInstr(String a, AtomicTemp l,AtomicTemp r,  LabelList j, int opcode) {
		super(a,null,l,r,0,j,opcode);
	}
	/*	public BRANCH(String a, Temp d, Temp l, Temp r,int c, Frame.Frame f,  int opcode) {
		super(a,d,l,r,c,f, opcode);
		}*/
}
