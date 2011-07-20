package assem;
import temp.*;
public class JumpInstr extends Oper
{
	public final static int J = 20;
	public JumpInstr(String a,  LabelList j, int opcode) {
		super(a,null,null, null, 0,j,opcode);
	}
}


