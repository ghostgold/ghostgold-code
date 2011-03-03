package Translate;
/** translate.Access */
public class Access {
    /** the level of function where the variable belongs */
    Level home;
    /** frame.Access of the variable to generate IR */
    Frame.Access acc;
    Access(Level h,Frame.Access a){
        home = h; 
		acc = a;
    }
	Tree.Exp exp(Tree.Exp framePtr){
		return acc.exp(framePtr);
	}
}