package absyn;
import symbol.Symbol;
public class IntExp extends Exp {
   public int value;
   public IntExp(int p, int v) {pos=p; value=v;}
	public IntExp clone(){
		return new IntExp(pos, value);
	}
}
