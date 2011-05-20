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
	public String toString(){return name;}
	
	public static Label label(String n, boolean num){
		String u = n.intern();
		Label s = (Label)dict.get(u);
		if (s==null) {
			s = new Label(u,num);
			dict.put(u,s);
		}
		return s;
	}
  /**
   * Makes a new label that prints as "name".
   * Warning: avoid repeated calls to <tt>new Label(s)</tt> with
   * the same name <tt>s</tt>.
   */
	public Label(String n, boolean num) {
		if(num)
			name=n + "_" + count;
		else 
			name = n;
		count++;
	}

  /**
   * Makes a new label with an arbitrary name.
   */
	public Label() {
		this("L" , true);
	}
	
  /**
   * Makes a new label whose name is the same as a symbol.
   */
	public Label(Symbol s, boolean num) {
		this(s.toString(), num);
	}
}
