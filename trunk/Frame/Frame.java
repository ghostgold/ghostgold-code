package Frame;
/**
 * 	this is an abstract interface of frame.Frame
 * 	the methods here shall be enough for you to complete IR phase 
 * 	you can add methods yourself if you need.
 */
public interface Frame {	
	/**
     * allocate a new variable of the frame
     * @param escape whether the variable has to be stored in memory
     * @return the frame.Access representation of the variable
     */
    public Access allocLocal( boolean escape );
    /**
     *  @return the temp of $fp
     */
    public Temp.Temp FP();
	
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
	public Frame newFrame( Temp.Label name ,  Util.BoolList fmls );
	
	/**
	 * get the list of arguments passed to the frame 
	 * @return the list of access of the arguments
	 */
	public AccessList getFormals();
	/**
	 * get the name of the frame
	 * @return the name of the frame 
	 */
	public Temp.Label getName();

	public int wordSize();
}