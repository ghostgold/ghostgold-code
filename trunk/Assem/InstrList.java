package Assem;

public class InstrList {
  public Instr head;
  public InstrList tail;
  public InstrList(Instr h, InstrList t) {
    head=h; tail=t;
  }
	public append(InstrList x){
		if(tail = null)tail = x;
		else tail.append(x);
	}
}
