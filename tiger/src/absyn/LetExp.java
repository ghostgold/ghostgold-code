package absyn;
import symbol.Symbol;
public class LetExp extends Exp {
   public DecList decs;
   public Exp body;
   public LetExp(int p, DecList d, Exp b) {pos=p; decs=d; body=b;}
	public LetExp clone(){
		return new LetExp(pos, decs.clone(), body.clone());
	}
}
