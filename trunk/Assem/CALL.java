package Assem;

public class CALL extends Instr
{
	Temp.TempList dsts;
	Temp.TempList srcs;
	Targets jump;

	public CALL(String a, Temp.TempList d, Temp.TempList s, Temp.LabelList j) {
		//		super(a,null,null, null, 0,j, -1);
		assem = a;
		dsts = d;
		srcs = s;
		jump=new Targets(j);
	}
	public Temp.TempList use(){
		return srcs;
	}
	public Temp.TempList  def(){
		return dsts;
	}
	public Targets jumps() {return jump;}

}
