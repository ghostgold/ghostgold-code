package absyn;
import symbol.Symbol;
public class StringExp extends Exp {
	public String value;
	public StringExp(int p, String v) {pos=p; value=v;}
	public StringExp clone(){
		return new StringExp(pos, value);
	}
}
