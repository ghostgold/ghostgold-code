package mips;
/**
 *	access implementation which indicates that the
 *	variable stays in a temp.Temp 
 */
class InReg implements frame.Access{
	@Override
	public tree.Exp exp( tree.Exp faddr ) {
		return new tree.Temp(temp);
	}
	temp.AtomicTemp temp;
	public InReg(){
		temp = new temp.AtomicTemp();
	}
	public boolean escape(){
		return false;
	}
	public int offSet(){
		return 0;
	}
}
