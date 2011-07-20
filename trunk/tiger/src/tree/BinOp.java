package tree;
import temp.AtomicLabel;
import temp.AtomicTemp;
public class BinOp extends Exp {
  public int binop;
  public Exp left, right;
  public BinOp(int b, Exp l, Exp r) {
    binop=b; left=l; right=r; 
  }
  public final static int PLUS=0, MINUS=1, MUL=2, DIV=3, 
		   AND=4,OR=5,LSHIFT=6,RSHIFT=7,ARSHIFT=8,XOR=9;
  public ExpList kids() {return new ExpList(left, new ExpList(right,null));}
  public Exp build(ExpList kids) {
    return new BinOp(binop,kids.head,kids.tail.head);
  }
}

