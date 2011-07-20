package assem;

public class MoveInstr extends Instr {
   public temp.AtomicTemp dst;   
   public temp.AtomicTemp src;

   public MoveInstr(String a, temp.AtomicTemp d, temp.AtomicTemp s) {
      assem=a; dst=d; src=s;
   }
   public temp.TempList use() {return new temp.TempList(src,null);}
   public temp.TempList def() {return new temp.TempList(dst,null);}
	public void setDef(temp.TempList d){dst = d.head;}
	public void setUse(temp.TempList s){src = s.head;};

   public Targets jumps()     {return null;}
	public String format(temp.TempMap m){
		String s = m.tempMap(src);
		String d = m.tempMap(dst);
		if(s.equals(d))return "";
		else return "move " + d + " " + s;
	}
}
