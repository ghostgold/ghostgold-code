package Assem;

public class MOVE extends Instr {
   public Temp.Temp dst;   
   public Temp.Temp src;

   public MOVE(String a, Temp.Temp d, Temp.Temp s) {
      assem=a; dst=d; src=s;
   }
   public Temp.TempList use() {return new Temp.TempList(src,null);}
   public Temp.TempList def() {return new Temp.TempList(dst,null);}
	public void setDef(Temp.TempList d){dst = d.head;}
	public void setUse(Temp.TempList s){src = s.head;};

   public Targets jumps()     {return null;}
	public String format(Temp.TempMap m){
		String s = m.tempMap(src);
		String d = m.tempMap(dst);
		if(s.equals(d))return "";
		else return "move " + d + " " + s;
	}
}
