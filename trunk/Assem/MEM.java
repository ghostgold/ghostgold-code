
package Assem;
import Temp.*;
public class MEM extends OPER
{
	public final static int LW = 30, SW = 31;
	/*	public BINOP(String a, Temp d, Temp l,Temp r, int c, Frame.Frame f,  LabelList j, int opcode) {
		super(a,d,l,r,c,f,j,opcode);
	}*/
	public Frame.Frame frame;
	public MEM(String a, Temp d, Temp l, Temp r,int c, Frame.Frame f, int opcode) {
		super(a,d,l,r,c,opcode);
		frame = f;
	}
}

