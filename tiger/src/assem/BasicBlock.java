package assem;
import java.util.*;
public class BasicBlock
{
	temp.TempList dst = null;
	temp.TempList src = null;
	temp.TempList temps = null;
	public InstrList instrs;
	public InstrList reverseInstrs;
	public temp.AtomicLabel label = null;
	public Instr last; 
	public BasicBlock(InstrList ins){
		instrs = ins;
		reverseInstrs = reverse(ins);
		if(ins.head instanceof LabelInstr){
			label = ((LabelInstr)instrs.head).label;
		}
		Set<temp.AtomicTemp> d = new HashSet();
		Set<temp.AtomicTemp> u = new HashSet();
		
		while(ins != null){
			last = ins.head;
			for(temp.TempList i = ins.head.use(); i != null; i= i.tail){
				if(!d.contains(i.head))u.add(i.head);
				temps = new temp.TempList(i.head, temps);
			}
			for(temp.TempList i = ins.head.def(); i != null; i = i.tail){
				if(!u.contains(i.head))d.add(i.head);
				temps = new temp.TempList(i.head, temps);
			}
			ins = ins.tail;
		}
		Iterator<temp.AtomicTemp> it = d.iterator();
		dst = null;
		src = null;
		while(it.hasNext()){
			dst = new temp.TempList(it.next(), dst);
		}
		it = u.iterator();
		while(it.hasNext()){
			src = new temp.TempList(it.next(), src);
		}
	}
	public ArrayList<Instr> toArrayListReverse(){
		ArrayList<Instr> ans = new ArrayList();
		for(InstrList i = reverseInstrs; i != null; i = i.tail){
			ans.add(i.head);
		}
		return ans;
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
		if(ins.head instanceof LabelInstr){
			label = ((LabelInstr)instrs.head).label;
		}
		Set<temp.AtomicTemp> d = new HashSet();
		Set<temp.AtomicTemp> u = new HashSet();
		
		while(ins != null){
			last = ins.head;
			for(temp.TempList i = ins.head.use(); i != null; i= i.tail){
				if(!d.contains(i.head))u.add(i.head);
				temps = new temp.TempList(i.head, temps);
			}
			for(temp.TempList i = ins.head.def(); i != null; i = i.tail){
				if(!u.contains(i.head))d.add(i.head);
				temps = new temp.TempList(i.head, temps);
			}
			ins = ins.tail;
		}
		dst = null;
		src = null;
		Iterator<temp.AtomicTemp> it = d.iterator();
		while(it.hasNext()){
			dst = new temp.TempList(it.next(), dst);
		}
		it = u.iterator();
		while(it.hasNext()){
			src = new temp.TempList(it.next(), src);
		}
	}

	public temp.TempList def(){
		return dst;
	}
	public temp.TempList use(){
		return src;
	}
	public temp.TempList tot(){
		return temps;
	}

	static public  ArrayList<BasicBlock> Partition(InstrList ins){
		ArrayList<BasicBlock> basicblocks = new  ArrayList();
		while(ins != null){
			InstrList block = new InstrList(ins.head, null);
			ins = ins.tail;
			InstrList tail = block;
			while(ins != null){
				if(ins.head instanceof LabelInstr)break;
				tail.tail = new InstrList(ins.head, null);
				tail = tail.tail;
				if(ins.head instanceof JumpInstr || ins.head instanceof BranchInstr){
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