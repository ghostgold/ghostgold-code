package frame;
/**
 * 	this is an abstract interface of frame.Frame
 * 	the methods here shall be enough for you to complete IR phase 
 * 	you can add methods yourself if you need.
 */
abstract public interface Frame {	
	/**
     * allocate a new variable of the frame
     * @param escape whether the variable has to be stored in memory
     * @return the frame.Access representation of the variable
     */
	public Access allocLocal( boolean escape );
    /**
     *  @return the temp of $fp
     */
	public temp.AtomicTemp FFP();
	public temp.AtomicTemp FP();
	public temp.AtomicTemp RV();
	public temp.AtomicTemp SP();
	public temp.AtomicTemp ZERO();
	public temp.AtomicTemp FORMAL(int x);
	public temp.AtomicTemp getReg(int x);
	public temp.TempList calleeSaves();
	public temp.TempList registers();
	public int framesize();
    /**
     * create a new frame the same type as current frame	
     * 	note: this method is a factory method
     * 
     * 	creation:	
     * 		in creation the Access of formals( arguments ) 
     * 		will be created by the frame, you can get them using the method getFormals  
     * @param name the name of the frame
     * @param fmls the boolean list that indicate whether each arguments escape
     * @return the frame created
     */
	public Frame newFrame( temp.AtomicLabel name ,  util.BoolList fmls , int out);
	
	/**
	 * get the list of arguments passed to the frame 
	 * @return the list of access of the arguments
	 */
	public AccessList getFormals();
	/**
	 * get the name of the frame
	 * @return the name of the frame 
	 */
	public temp.AtomicLabel getName();

	public int wordSize();
	
	public tree.Exp externalCall(String func, tree.ExpList args);
	
	public temp.TempList calldefs();
	
	public temp.TempList syscalldefs();

	public temp.TempList parameters();

	public tree.Stm procEntryExit1(tree.Stm body);
	
	public assem.InstrList procEntryExit2(assem.InstrList body);
	
	public assem.InstrList procEntryExit3(assem.InstrList body);

	public assem.InstrList codegen(tree.Stm stm);
}
