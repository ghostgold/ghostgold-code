package frame;
/** interface of Access:represent a variable */
public interface Access {
    /**
     * get the IR of the variable the access represent
     * @param faddr the  frame address of the variable
     * @return the IR of the variable
     */
    public tree.Exp exp( tree.Exp faddr );
	public boolean escape();
	public int offSet();
}