package assem;

public class InstrList {
  public Instr head;
  public InstrList tail;
  public InstrList(Instr h, InstrList t) {
    head=h; tail=t;
  }
	public void append(InstrList x){
		if(tail == null)tail = x;
		else tail.append(x);

	}
	public void append(Instr x){
		if(tail == null)tail = new InstrList(x, null);
		else tail.append(x);
	}
}
