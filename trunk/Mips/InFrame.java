package Mips;
/**
 *	access implementation which indicates that the
 *	variable stays in memory
 */
public class InFrame implements Frame.Access{
	@Override
	public tree.Exp exp( tree.Exp faddr ) {
		return new tree.MEM(new tree.BINOP(BINOP.PLUS, faddr, new tree.CONST(offset)));
	}
	int offset;
	public InFrame(int o){
		offset = o;
	}
}
