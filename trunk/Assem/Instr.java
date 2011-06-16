package Assem;
import Temp.*;
public abstract class Instr {
	//	public final static int ADD=0, SUB=1, MUL=2, DIV=3, LW = 4, SW = 5, BEQ = 6 ;
	public boolean dead;
	public int opcode;
	public String assem;
	public int constant;
	public String toString(){
		return "";
	}
	public abstract TempList use();
	public abstract TempList def();
	//	public abstract void setUse(Temp.TempList s);
	//	public abstract void setDef(Temp.TempList d);
	public abstract Targets jumps();

	private Temp nthTemp(TempList l, int i) {
		if (i==0) return l.head;
		else return nthTemp(l.tail,i-1);
	}

	private Label nthLabel(LabelList l, int i) {
		if (i==0) return l.head;
		else return nthLabel(l.tail,i-1);
	}

	public String format(TempMap m) {
		TempList dst = def();
		TempList src = use();
		Targets j = jumps();
		LabelList jump = (j==null)?null:j.labels;
		StringBuffer s = new StringBuffer();
		int len = assem.length();
		for(int i=0; i<len; i++)
			if (assem.charAt(i)=='`')
				switch(assem.charAt(++i)) {
				case 's': {
					int n = Character.digit(assem.charAt(++i),10);
					s.append(m.tempMap(nthTemp(src,n)));
				}
					break;
				case 'd': {
					int n = Character.digit(assem.charAt(++i),10);
					s.append(m.tempMap(nthTemp(dst,n)));
				}
					break;
				case 'j': {
					int n = Character.digit(assem.charAt(++i),10);
					s.append(nthLabel(jump,n).toString());
				}
					break;
				case 'i':{
					s.append(Integer.toString(constant));
				}
					break;
				case '`': s.append('`'); 
					break;
				default: {
					//					System.out.println(assem);
					throw new Error("bad Assem format");
				}
				}
			else s.append(assem.charAt(i));
		return s.toString();
	}
}
