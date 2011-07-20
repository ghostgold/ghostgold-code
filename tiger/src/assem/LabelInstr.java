package assem;

public class LabelInstr extends Instr {
   public temp.AtomicLabel label;

   public LabelInstr(String a, temp.AtomicLabel l) {
      assem=a; label=l;
   }

   public temp.TempList use() {return null;}
   public temp.TempList def() {return null;}
	public void setDef(temp.TempList d){}
	public void  setUse(temp.TempList s){}

   public Targets jumps()     {return null;}

}
