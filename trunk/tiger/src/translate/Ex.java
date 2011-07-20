package translate;
/**
 * 	this is one implementation of translate.Exp
 * 	this implementation represents a class of 'value'
 * 	typed expressionsx 
 */
public class Ex extends Exp{
	private tree.Exp exp;
	public Ex( tree.Exp exp ){
		this.exp = exp;
	}
	@Override
	public tree.Exp unEx() {
		return exp;
	}
	@Override
	public tree.Stm unNx() {
		return new tree.ExpNoValue(exp);
	}
	@Override
	public tree.Stm unCx( temp.AtomicLabel iftrue, temp.AtomicLabel iffalse ) {
		return new tree.Cjump(tree.Cjump.EQ, exp, new tree.Const(0), iffalse, iftrue);
	}
}
