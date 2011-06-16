package RegAlloc;
public class CopyPair
{
	Temp.Temp from;
	Temp.Temp to;
	public CopyPair(Temp.Temp t, Temp.Temp f){
		to = t;
		from = f;
	}
	public CopyPair(Assem.MOVE m){
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