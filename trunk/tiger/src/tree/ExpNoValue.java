package tree;
import temp.AtomicLabel;
import temp.AtomicTemp;
public class ExpNoValue extends Stm {
  public Exp exp; 
  public ExpNoValue(Exp e) {exp=e;}
  public ExpList kids() {return new ExpList(exp,null);}
  public Stm build(ExpList kids) {
    return new ExpNoValue(kids.head);
  }
}

