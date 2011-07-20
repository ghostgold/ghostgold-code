package tree;
import temp.AtomicLabel;
import temp.LabelList;
import temp.AtomicTemp;
public class Jump extends Stm {
  public Exp exp;
  public LabelList targets;
  public Jump(Exp e, LabelList t) {exp=e; targets=t;}
  public Jump(AtomicLabel target) {
      this(new Name(target), new LabelList(target,null));
  }
  public ExpList kids() {return new ExpList(exp,null);}
  public Stm build(ExpList kids) {
    return new Jump(kids.head,targets);
  }
}

