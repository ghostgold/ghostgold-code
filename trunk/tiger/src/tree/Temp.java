package tree;

public class Temp extends Exp {
  public temp.AtomicTemp temp;
  public Temp(temp.AtomicTemp t) {temp=t;}
  public ExpList kids() {return null;}
  public Exp build(ExpList kids) {return this;}
}

