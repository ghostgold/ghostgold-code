package Mips;
/**
 *	access implementation which indicates that the
 *	variable stays in memory
 */
public class InFrame implements Frame.Access{
	@Override
	public Tree.Exp exp( tree.Exp faddr ) {
		return new Tree.MEM(new Tree.BINOP(BINOP.PLUS, faddr, new Tree.CONST(offset)));
	}
	int offset;
	public InFrame(int o){
		offset = o;
	}
}
