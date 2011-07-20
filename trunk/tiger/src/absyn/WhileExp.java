package absyn;
import symbol.Symbol;
public class WhileExp extends Exp {
   public Exp test, body;
   public WhileExp(int p, Exp t, Exp b) {pos=p; test=t; body=b;}
	public WhileExp clone(){
		return new WhileExp(pos, test.clone(), body.clone());
	}
}
