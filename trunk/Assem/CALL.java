package Assem;

public class CALL extends Instr {
   public Temp.TempList dst;   
   public Temp.TempList src;
   public Targets jump;

   public CALL(String a, Temp.TempList d, Temp.TempList s, Temp.LabelList j) {
      assem=a; dst=d; src=s; jump=new Targets(j);
   }
   public CALL(String a, Temp.TempList d, Temp.TempList s) {
      assem=a; dst=d; src=s; jump=null;
   }

   public Temp.TempList use() {return src;}
   public Temp.TempList def() {return dst;}
	public void setDef(Temp.TempList d){dst = d;}
	public void setUse(Temp.TempList s){src = s;};
   public Targets jumps() {return jump;}

}
