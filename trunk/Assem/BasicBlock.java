package Assem;
import java.util.*;
public class BasicBlock
{
	Temp.TempList dst = null;
	Temp.TempList src = null;
	Temp.TempList temps = null;

	public InstrList instrs;
	public InstrList reverseInstrs;
	public Temp.Label label = null;
	public Instr last; 
	public BasicBlock(InstrList ins){
		instrs = ins;
		reverseInstrs = reverse(ins);
		if(ins.head instanceof LABEL){
			label = ((LABEL)instrs.head).label;
		}
		Set<Temp.Temp> d = new HashSet();
		Set<Temp.Temp> u = new HashSet();
		
		while(ins != null){
			last = ins.head;
			for(Temp.TempList i = ins.head.use(); i != null; i= i.tail){
				if(!d.contains(i.head))u.add(i.head);
				temps = new Temp.TempList(i.head, temps);
			}
			for(Temp.TempList i = ins.head.def(); i != null; i = i.tail){
				if(!u.contains(i.head))d.add(i.head);
				temps = new Temp.TempList(i.head, temps);
			}
			ins = ins.tail;
		}
		Iterator<Temp.Temp> it = d.iterator();
		dst = null;
		src = null;
		while(it.hasNext()){
			dst = new Temp.TempList(it.next(), dst);
		}
		it = u.iterator();
		while(it.hasNext()){
			src = new Temp.TempList(it.next(), src);
		}
	}
	InstrList reverse(InstrList ins){
		InstrList ans = null;
		while(ins != null){
			ans = new InstrList(ins.head, ans);
			ins = ins.tail;
		}
		return ans;
	}
	public void  setInstrs(InstrList ins){
		instrs = ins;
		reverseInstrs = reverse(ins);
		if(ins.head instanceof LABEL){
			label = ((LABEL)instrs.head).label;
		}
		Set<Temp.Temp> d = new HashSet();
		Set<Temp.Temp> u = new HashSet();
		
		while(ins != null){
			last = ins.head;
			for(Temp.TempList i = ins.head.use(); i != null; i= i.tail){
				if(!d.contains(i.head))u.add(i.head);
				temps = new Temp.TempList(i.head, temps);
			}
			for(Temp.TempList i = ins.head.def(); i != null; i = i.tail){
				if(!u.contains(i.head))d.add(i.head);
				temps = new Temp.TempList(i.head, temps);
			}
			ins = ins.tail;
		}
		dst = null;
		src = null;
		Iterator<Temp.Temp> it = d.iterator();
		while(it.hasNext()){
			dst = new Temp.TempList(it.next(), dst);
		}
		it = u.iterator();
		while(it.hasNext()){
			src = new Temp.TempList(it.next(), src);
		}
	}

	public Temp.TempList def(){
		return dst;
	}
	public Temp.TempList use(){
		return src;
	}
	public Temp.TempList tot(){
		return temps;
	}
	static public  ArrayList<BasicBlock> Partition(InstrList ins){
		ArrayList<BasicBlock> basicblocks = new  ArrayList();
		while(ins != null){
			InstrList block = new InstrList(ins.head, null);
			ins = ins.tail;
			InstrList tail = block;
			while(ins != null){
				if(ins.head instanceof LABEL)break;
				tail.tail = new InstrList(ins.head, null);
				tail = tail.tail;
				if(ins.head instanceof JUMP || ins.head instanceof BRANCH){
					ins = ins.tail;
					break;
				}
				ins = ins.tail;

			}
			basicblocks.add(new BasicBlock(block));
		}
		return basicblocks;
	}
   
}