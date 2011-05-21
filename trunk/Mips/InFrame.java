package Mips;
/**
 *	access implementation which indicates that the
 *	variable stays in memory
 */
public class InFrame implements Frame.Access{
	@Override
	public Tree.Exp exp( Tree.Exp faddr ) {
		return new Tree.MEM(new Tree.BINOP(Tree.BINOP.PLUS, faddr, new Tree.CONST(offset)));
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
