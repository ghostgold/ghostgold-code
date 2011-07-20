package translate;
/** translate.Access */
public class Access {
    /** the level of function where the variable belongs */
    Level home;
    /** frame.Access of the variable to generate IR */
    frame.Access acc;
    Access(Level h,frame.Access a){
        home = h; 
		acc = a;
    }
	tree.Exp exp(tree.Exp framePtr){
		return acc.exp(framePtr);
	}
}