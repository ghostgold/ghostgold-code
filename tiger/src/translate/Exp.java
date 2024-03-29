package translate;
/**
 * 	This class is the base interface of translate.Exp
 * 	which can generates IR as you need 
 * 
 * 	comment:
 * 		the use of translate.Exp is called strategy pattern.
 *		which provide abstract define of strategy and require
 *		each subclass to provide specific implementation.	 
 *		@see the book Design Pattern for detail	
 */
public abstract class Exp {
	/**
	 * get the value of this expression
	 * @return the value IR representation 
	 */
	public boolean high = false;
	public abstract tree.Exp unEx();
	/**
	 * get the action of this expression
	 * @return the statement IR representation
	 */
	public abstract tree.Stm unNx();
	/**
	 * create a statement that act as follows:
	 * 		if this expression is true, 
	 * 			then jump to label iftrue
	 * 			else jump to label iffalse
	 * @param iftrue 	true label
	 * @param iffalse	false label
	 * @return	the statement created
	 */
	public abstract tree.Stm unCx( temp.AtomicLabel iftrue , temp.AtomicLabel iffalse );
}
