package mips;
/**
 *	access implementation which indicates that the
 *	variable stays in memory
 */
public class InFrame implements frame.Access{
	@Override
	public tree.Exp exp( tree.Exp faddr ) {
		return new tree.Mem(new tree.BinOp(tree.BinOp.PLUS, faddr, new tree.Const(offset)));
	}
	int offset;
	public InFrame(int o){
		offset = o;
	}
	public boolean escape(){
		return true;
	}
	public int offSet(){
		return offset;
	}
}
