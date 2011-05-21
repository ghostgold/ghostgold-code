package Assem;

public class LABEL extends Instr {
   public Temp.Label label;

   public LABEL(String a, Temp.Label l) {
      assem=a; label=l;
   }

   public Temp.TempList use() {return null;}
   public Temp.TempList def() {return null;}
	public void setDef(Temp.TempList d){}
	public void  setUse(Temp.TempList s){}

   public Targets jumps()     {return null;}

}
