package Absyn;
import Symbol.Symbol;
public class LetExp extends Exp {
   public DecList decs;
   public ExpList body;
   public LetExp(int p, DecList d, ExpList b) {pos=p; decs=d; body=b;}
}
