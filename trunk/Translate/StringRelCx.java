package Translate;
import Temp.Label;
public class StringRelCx extends Cx{

	int rel;
	Tree.Exp left, right;
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
	public Tree.Stm unCx(Label iftrue, Label iffalse){
		Tree.Stm move = new Tree.SEQ(new Tree.MOVE(new Tree.TEMP(level.frame.FORMAL(0)), left), 
									 new Tree.MOVE(new Tree.TEMP(level.frame.FORMAL(0)), right));
		Tree.Exp call = level.frame.externalCall("strcmp", 
												  new Tree.ExpList(new Tree.TEMP(level.frame.FORMAL(0)),
																   new Tree.ExpList(new Tree.TEMP(level.frame.FORMAL(1)), 
																					null)));
		Tree.CJUMP cjump = new Tree.CJUMP(rel, call, new Tree.CONST(0), iftrue, iffalse);
		return new Tree.SEQ(move, cjump);
	}

	/*	public Tree.Stm unNx(){
		return new Tree.EXP(unEx());
		}*/

	public StringRelCx(int t, Tree.Exp l, Tree.Exp r, Level lev){
		rel = t;
		left = l;
		right = r;
		level = lev;
	}
}