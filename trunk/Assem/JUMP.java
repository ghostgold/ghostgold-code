package Assem;
import Temp.*;
public class JUMP extends OPER
{
	public final static int J = 20;
	public JUMP(String a,  LabelList j, int opcode) {
		super(a,null,null, null, 0,j,opcode);
	}
}


