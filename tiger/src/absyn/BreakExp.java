package absyn;
import symbol.Symbol;
public class BreakExp extends Exp {
   public BreakExp(int p) {pos=p;}
	public BreakExp clone(){
		return new BreakExp(pos);
	}
}
