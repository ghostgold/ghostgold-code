package Assem;
import Temp.*;
public class OPER extends Instr 
{
	//	Temp.TempList dst;   
	//	Temp.TempList src;
	public Temp dst;
	public Temp left, right;
	public TempList dsts;
	public TempList srcs;
	Targets jump;
	public int opcode;
	/*	public OPER(String a, Temp.TempList d, Temp.TempList s, Temp.LabelList j) {
		assem=a; dst=d; src=s; jump=new Targets(j);
	}
	public OPER(String a, Temp.TempList d, Temp.TempList s) {
		assem=a; dst=d; src=s; jump=null;
		}*/
	public OPER(String a, Temp d, Temp l, Temp r, int c, LabelList j, int op) {
		assem=a; 
		dst = d;
		left = l;
		right = r;
		super.constant = c;
		opcode = op;
		jump=new Targets(j);
		if(left != null){
			if(right == null)srcs= new TempList(left);
			else srcs = new  TempList(left, new TempList(right));
		}
		else{
			if(right == null)srcs =  null;
			else srcs =  new TempList(right);
		}
		if(dst != null)dsts = new TempList(dst);
		else dsts =  null;
	}
	public OPER(String a, Temp d, Temp l, Temp r, int c,  int op) {
		assem=a; 
		dst = d;
		left = l;
		right = r;
		super.constant = c;
		opcode = op;
		if(left != null){
			if(right == null)srcs= new TempList(left);
			else srcs = new  TempList(left, new TempList(right));
		}
		else{
			if(right == null)srcs =  null;
			else srcs =  new TempList(right);
		}
		if(dst != null)dsts = new TempList(dst);
		else dsts =  null;

	}
	
	public void setDst(Temp d){
		dst = d;
		if(dst == null)dsts = null;
		else dsts = new  TempList(dst);
	}

	public void setSrc(Temp l ,Temp r){
		left = l;
		right = r;
		if(left != null){
			if(right == null)srcs= new TempList(left);
			else srcs = new  TempList(left, new TempList(right));
		}
		else{
			if(right == null)srcs =  null;
			else srcs =  new TempList(right);
		}
	}
	public TempList use() {
		return srcs;
		/*		if(left != null){
			if(right == null)return new TempList(left);
			else return new TempList(left, new TempList(right));
		}
		else{
			if(right == null)return null;
			else return new TempList(right);
			}*/
	}
	public TempList def() {
		return dsts;
		/*		if(dst != null)return new TempList(dst);
				else return null;*/
	}
	/*	public void setDef(Temp.Temp a, Temp.Temp b){
		OprandList d = opdst;
		while(d != null){
			if(d.head.toTemp() == a){
				((Reg)(d.head)).reg = b;
				break;
			}
			d = d.tail;
		}
	}
	public void setUse(Temp.Temp a, Temp.Temp b){
		OprandList s = opsrc;
		while(s != null){
			if(s.head.toTemp() == a){
				((Reg)(s.head)).reg = b;
				break;
			}
			s = s.tail;
		}
		}*/
	/*	public void setDef(Temp.TempList d){
		OprandList oprand = opdst;
		dst = d;
		while(d != null && oprand != null){
			if(oprand.head instanceof Reg){
				(Reg)(oprand.head).reg = d.head;
				d = d.tail;
			}
			oprand = oprand.tail;
		}
	}
	public void setUse(Temp.TempList s){
		OprandList oprand = opsrc;
		src = s;
		while(s != null && oprand != null){
			if(oprand.head instanceof Reg){
				(Reg)(oprand.head).reg = s.head;
				s = s.tail;
			}
			oprand = oprand.tail;
		}
		}*/
	public Targets jumps() {return jump;}
}
