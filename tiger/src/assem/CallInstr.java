package assem;

public class CallInstr extends Instr
{
	temp.TempList dsts;
	temp.TempList srcs;
	Targets jump;
	temp.AtomicLabel name;
	public CallInstr(String a, temp.TempList d, temp.TempList s, temp.AtomicLabel name) {
		//		super(a,null,null, null, 0,j, -1);
		assem = a;
		dsts = d;
		srcs = s;
		if(name == null)
			jump = new Targets(null);
		else 
			jump=new Targets(new temp.LabelList(name, null));
	}
	public temp.TempList use(){
		return srcs;
	}
	public temp.TempList  def(){
		return dsts;
	}
	public Targets jumps() {return jump;}

}
