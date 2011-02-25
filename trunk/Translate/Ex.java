package Translate;
/**
 * 	this is one implementation of translate.Exp
 * 	this implementation represents a class of 'value'
 * 	typed expressionsx 
 */
public class Ex extends Exp{
	private Tree.Exp exp;
	public Ex( Tree.Exp exp ){
		this.exp = exp;
	}
	@Override
	public Tree.Exp unEx() {
		return exp;
	}
	@Override
	public Tree.Stm unNx() {
		return new Tree.EXP(exp);
	}
	@Override
	public Tree.Stm unCx( Temp.Label iftrue, Temp.Label iffalse ) {
		return new Tree.CJUMP(Tree.CJUMP.EQ, exp, new Tree.CONST(0), iffalse, iftrue);
	}
}
