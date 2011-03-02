package Translate;
/**
 * 	this is one implementation of translate.Exp
 * 	this implementation represents a class of 'action'
 * 	typed expression
 */
public class Nx extends Exp{
	private Tree.Stm stm;
	public Nx( Tree.Stm stm ){
		this.stm = stm;
	}
	@Override
	public Tree.Exp unEx() {
		throw new Error("Nx have no value");
	}
	@Override
	public Tree.Stm unNx() {
		return stm;
	}
	@Override
	public Tree.Stm unCx( Temp.Label iftrue, Temp.Label iffalse ) {
		throw new Error("Nx have no value");
	}
}
