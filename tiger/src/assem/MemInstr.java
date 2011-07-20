
package assem;
import temp.*;
public class MemInstr extends Oper
{
	public boolean neverChange = false;
	public final static int LW = 30, SW = 31;
	/*	public BINOP(String a, Temp d, Temp l,Temp r, int c, Frame.Frame f,  LabelList j, int opcode) {
		super(a,d,l,r,c,f,j,opcode);
	}*/
	public frame.Frame frame;
	public MemInstr(String a, AtomicTemp d, AtomicTemp l, AtomicTemp r,int c, frame.Frame f, int opcode) {
		super(a,d,l,r,c,opcode);
		frame = f;
	}
	public String toString(){
		if(opcode == LW){
			if(neverChange) return  "lw " + constant + "(" + left.toString() + ")nc";
			return "lw " + constant + "(" + left.toString() + ")";
		}
		else
			return "---";
	}
	public boolean aboutStack(){
		if(right == frame.FP() || right == frame.SP() || right == frame.FFP())return true;
		return false;
	}
	public boolean killedBySwOrCall(Instr i){
		if(i.opcode != MemInstr.SW && !( i instanceof CallInstr))
			return false;
		if(this.neverChange)return false;
		if(i.opcode == MemInstr.SW){
			MemInstr mem = (MemInstr)i;
			if(this.aboutStack() ^ mem.aboutStack())return false;
			else return true;
		}
		else return true;
	}

}

