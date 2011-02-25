package Translate;
/**
 * 	this is one implementation of translate.Exp
 * 	this implementation represents a class of 'action'
 * 	typed expression
 */
public class Nx extends Exp{
	private tree.Stm stm;
	public Nx( tree.Stm stm ){
		this.stm = stm;
	}
	@Override
	public tree.Exp unEx() {
		throw new Error("Nx have no value");
	}
	@Override
	public tree.Stm unNx() {
		return stm;
	}
	@Override
	public tree.Stm unCx( temp.Label iftrue, temp.Label iffalse ) {
		throw new Error("Nx have no value");
	}
}
