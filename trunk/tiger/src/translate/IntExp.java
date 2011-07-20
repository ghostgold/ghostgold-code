package translate;
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
	public tree.Exp unEx() {
		return new tree.Const( value );
	}
	@Override
	public tree.Stm unNx() {
		return new tree.ExpNoValue(this.unEx());
	}
	@Override
	public tree.Stm unCx( temp.AtomicLabel iftrue, temp.AtomicLabel iffalse ) {
		if( value != 0 ) 
			return new tree.Jump( iftrue );
		else 
			return new tree.Jump( iffalse );
	}
}
