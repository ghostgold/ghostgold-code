package translate;
import temp.AtomicLabel;
public class RelCx extends Cx{

	int rel;
	tree.Exp left, right;
	public tree.Stm unCx(AtomicLabel iftrue, AtomicLabel iffalse){
		return new tree.Cjump(rel, left, right, iftrue, iffalse);
	}
	public RelCx(int t, tree.Exp l, tree.Exp r){
		rel = t;
		left = l;
		right = r;
	}
}