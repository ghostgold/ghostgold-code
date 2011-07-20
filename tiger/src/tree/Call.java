package tree;
import temp.AtomicLabel;
import temp.AtomicTemp;
public class Call extends Exp {
  public Exp func;
  public ExpList args;
	public int argnum;

  public Call(Exp f, ExpList a) {func=f; args=a;}
  public ExpList kids() {return new ExpList(func,args);}
  public Exp build(ExpList kids) {
    return new Call(kids.head,kids.tail);
  }
  
}

