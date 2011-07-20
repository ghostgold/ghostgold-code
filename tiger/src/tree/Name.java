package tree;
import temp.AtomicLabel;
import temp.AtomicTemp;
public class Name extends Exp {
  public AtomicLabel label;
  public Name(AtomicLabel l) {label=l;}
  public ExpList kids() {return null;}
  public Exp build(ExpList kids) {return this;}
}

