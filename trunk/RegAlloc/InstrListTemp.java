package RegAlloc;
import Assem.*;
import Temp.*;
public class InstrListTemp
{
	InstrList instr;
	Temp temp;
	public InstrListTemp(InstrList i,  Temp t){
		instr = i;
		temp = t;
	}
	public InstrListTemp(InstrList i){
		instr = i;
		temp = null;
	}
}