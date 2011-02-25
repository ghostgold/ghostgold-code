package Translate;
/**
 * 	this is one implementation of translate.Exp
 * 	constant integer expression,
 * 	
 * 	this code is an example of how can we optimize the code
 * 		see unCx
 */
public class IntExp extends Exp{
	private int value;
	public IntExp( int value ){
		this.value = value;
	}
	@Override
	public Tree.Exp unEx() {
		return new Tree.CONST( value );
	}
	@Override
	public Tree.Stm unNx() {
		return new Tree.EXP(this.unEx());
	}
	@Override
	public Tree.Stm unCx( Temp.Label iftrue, Temp.Label iffalse ) {
		if( value != 0 ) 
			return new Tree.JUMP( iftrue );
		else 
			return new Tree.JUMP( iffalse );
	}
}
