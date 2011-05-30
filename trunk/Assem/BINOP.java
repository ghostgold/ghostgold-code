
package Assem;
import Temp.*;
public class BINOP extends OPER
{
	public final static int ADD = 0, ADDI = 1, SUB = 2, MUL = 3, DIV = 4, LI = 5, LA = 6;
	/*	public BINOP(String a, Temp d, Temp l,Temp r, int c, Frame.Frame f,  LabelList j, int opcode) {
		super(a,d,l,r,c,f,j,opcode);
		}*/
	public BINOP(String a, Temp d, Temp l, Temp r,int c,  int opcode) {
		super(a,d,l,r,c, opcode);
	}
}

