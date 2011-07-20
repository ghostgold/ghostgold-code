package regalloc;
public class CopyPair
{
	temp.AtomicTemp from;
	temp.AtomicTemp to;
	public CopyPair(temp.AtomicTemp t, temp.AtomicTemp f){
		to = t;
		from = f;
	}
	public CopyPair(assem.MoveInstr m){
		from = m.src;
		to = m.dst;
	}
	public boolean nequals(Object c){
		if(!(c instanceof CopyPair))return false;
		
		if(((CopyPair)c).from == from && ((CopyPair)c).to == to)return true;
		return false;
	}
	public int hashCode(){
		return from.hashCode()*2333 + to.hashCode();
	}
}