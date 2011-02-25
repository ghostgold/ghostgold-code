package Mips;
/**
 *	access implementation which indicates that the
 *	variable stays in a temp.Temp 
 */
class InReg implements Frame.Access{
	@Override
	public tree.Exp exp( tree.Exp faddr ) {
		return new tree.TEMP(temp);
	}
	Temp.Temp temp;
	public InReg(){
		temp = new Temp.Temp();
	}
}
