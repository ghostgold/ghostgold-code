package Assem;

public class CALL extends Instr
{
	Temp.TempList dsts;
	Temp.TempList srcs;
	Targets jump;
	Temp.Label name;
	public CALL(String a, Temp.TempList d, Temp.TempList s, Temp.Label name) {
		//		super(a,null,null, null, 0,j, -1);
		assem = a;
		dsts = d;
		srcs = s;
		if(name == null)
			jump = new Targets(null);
		else 
			jump=new Targets(new Temp.LabelList(name, null));
	}
	public Temp.TempList use(){
		return srcs;
	}
	public Temp.TempList  def(){
		return dsts;
	}
	public Targets jumps() {return jump;}

}
