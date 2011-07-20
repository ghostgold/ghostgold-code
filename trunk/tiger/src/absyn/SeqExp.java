package absyn;
import symbol.Symbol;
public class SeqExp extends Exp {
   public ExpList list;
   public SeqExp(int p, ExpList l) {pos=p; list=l;}
	public SeqExp clone(){
		if(list != null)return new SeqExp(pos, list.clone());
		else return new SeqExp(pos, null);
	}
}
