package tree;

public class Const extends Exp {
  public int value;
  public Const(int v) {value=v;}
  public ExpList kids() {return null;}
  public Exp build(ExpList kids) {return this;}
}

