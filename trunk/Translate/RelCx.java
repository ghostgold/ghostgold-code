package Translate;
import Temp.Label;
public class RelCx extends Cx{
	int rel;
	Tree.Exp left, right;
	public Tree.Stm unCx(Label iftrue, Label iffalse){
		return new Tree.CJUMP(rel, left, right, iftrue, iffalse);
	}
}