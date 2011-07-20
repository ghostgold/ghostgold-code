package tree;
import temp.AtomicLabel;
import temp.AtomicTemp;
public class Seq extends Stm {
  public Stm left, right;
  public Seq(Stm l, Stm r) { left=l; right=r; }
  public ExpList kids() {throw new Error("kids() not applicable to SEQ");}
  public Stm build(ExpList kids) {throw new Error("build() not applicable to SEQ");}
}

