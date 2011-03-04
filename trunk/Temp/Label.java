package Temp;
import Symbol.Symbol;

/**
 * A Label represents an address in assembly language.
 */

public class Label  {
	private String name;
	private static int count;
	private static java.util.Dictionary dict = new java.util.Hashtable();
  /**
   * a printable representation of the label, for use in assembly 
   * language output.
   */
	public String toString() {return name;}
	public static Label label(String n){
		String u = n.intern();
		Label s = (Label)dict.get(u);
		if (s==null) {
			s = new Label(u);
			dict.put(u,s);
		}
		return s;
	}
  /**
   * Makes a new label that prints as "name".
   * Warning: avoid repeated calls to <tt>new Label(s)</tt> with
   * the same name <tt>s</tt>.
   */
	public Label(String n) {
		name=n;
	}

  /**
   * Makes a new label with an arbitrary name.
   */
   public Label() {
	this("L" + count++);
   }
	
  /**
   * Makes a new label whose name is the same as a symbol.
   */
	public Label(Symbol s) {
		this(s.toString());
	}
}
