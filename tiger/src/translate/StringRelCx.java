package translate;
import temp.AtomicLabel;
public class StringRelCx extends Cx{

	int rel;
	tree.Exp left, right;
	Level level;
	/*	public Tree.Exp unEx(){
		
		switch(rel){
		case Tree.CJUMP.EQ:
			return level.frame.externalCall("stringEq", new Tree.ExpList(left,
														 new Tree.ExpList(right, null)));
		case Tree.CJUMP.NE:
			return level.frame.externalCall("stringNe", new Tree.ExpList(left,
														 new Tree.ExpList(right, null)));
		case Tree.CJUMP.LT:
			return level.frame.externalCall("stringLt", new Tree.ExpList(left,
														 new Tree.ExpList(right, null)));
		case Tree.CJUMP.LE:
			return level.frame.externalCall("stringLe", new Tree.ExpList(left,
														 new Tree.ExpList(right, null)));
		case Tree.CJUMP.GT:
			return level.frame.externalCall("stringGt", new Tree.ExpList(left,
														 new Tree.ExpList(right, null)));
		case Tree.CJUMP.GE:
			return level.frame.externalCall("stringGe", new Tree.ExpList(left,
														 new Tree.ExpList(right, null)));
		default:
			return null;
		}
		}*/
	public tree.Stm unCx(AtomicLabel iftrue, AtomicLabel iffalse){
		temp.AtomicTemp a0 = new temp.AtomicTemp();
		tree.Stm move = new tree.Seq(new tree.Move(new tree.Temp(a0), left), 
									 new tree.Move(new tree.Temp(level.frame.FORMAL(1)), right));
		move = new tree.Seq(move, new tree.Move(new tree.Temp(level.frame.FORMAL(0)), new tree.Temp(a0)));
		tree.Exp call = level.frame.externalCall("strcmp", 
												  new tree.ExpList(new tree.Temp(level.frame.FORMAL(0)),
																   new tree.ExpList(new tree.Temp(level.frame.FORMAL(1)), 
																					null)));
		tree.Cjump cjump = new tree.Cjump(rel, call, new tree.Const(0), iftrue, iffalse);
		return new tree.Seq(move, cjump);
	}

	/*	public Tree.Stm unNx(){
		return new Tree.EXP(unEx());
		}*/

	public StringRelCx(int t, tree.Exp l, tree.Exp r, Level lev){
		rel = t;
		left = l;
		right = r;
		level = lev;
	}
}