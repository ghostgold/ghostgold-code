package regalloc;
import temp.*;
import assem.*;
public class InstrListTemp
{
	InstrList instr;
	AtomicTemp temp;
	public InstrListTemp(InstrList i,  AtomicTemp t){
		instr = i;
		temp = t;
	}
	public InstrListTemp(InstrList i){
		instr = i;
		temp = null;
	}
}