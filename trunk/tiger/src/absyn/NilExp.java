package absyn;
import symbol.Symbol;
public class NilExp extends Exp {
  public NilExp(int p) {pos=p;}
	public NilExp clone(){
		return new NilExp(pos);
	}
}
