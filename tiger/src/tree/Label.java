package tree;

public class Label extends Stm { 
  public temp.AtomicLabel label;
  public Label(temp.AtomicLabel l) {label=l;}
  public ExpList kids() {return null;}
  public Stm build(ExpList kids) {
    return this;
  }
}

