package Mips;
/**
 *	access implementation which indicates that the
 *	variable stays in a temp.Temp 
 */
class InReg implements Frame.Access{
	@Override
	public Tree.Exp exp( Tree.Exp faddr ) {
		return new Tree.TEMP(temp);
	}
	Temp.Temp temp;
	public InReg(){
		temp = new Temp.Temp();
	}
}
