package Translate;
import Temp.Label;
public class RelCx extends Cx{

	int rel;
	Tree.Exp left, right;
	public Tree.Stm unCx(Label iftrue, Label iffalse){
		return new Tree.CJUMP(rel, left, right, iftrue, iffalse);
	}
	public RelCx(int t, Tree.Exp l, Tree.Exp r){
		rel = t;
		left = l;
		right = r;
	}
}