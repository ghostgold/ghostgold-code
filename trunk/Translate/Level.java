package Translate;
/** translate.Level */

import Util.BoolList;
public class Level {
    /** the frame of current level */
    Frame.Frame         frame;
    /** parent level */
    public Level        parent;
	/**
     * allocate a new variable of the level
     * @param escape whether the variable has to be stored in memory
     * @return the translate.Access representation of the variable
     */
	public Level(Level p, Temp.Label n, BoolList f){
		parent = p;
		frame = p.frame.newFrame(n, new BoolList(true, f));
	}

	public Level(Frame.Frame f){
		frame = f;
	}

    public Access allocLocal( boolean escape ){
		return new Access(this, frame.allocLocal(escape));
    }
	
	public Access staticLink(){
		return new Access(this, frame.getFormals().head);
	}
    /**
     * get the IR of frame address of target level in current level
     * @param target target level
     * @return the IR representation
     */
    public Tree.Exp getFPOf( Level target ){
    	//TODO implement me
		Level tLevel = this;
		Tree.Exp ans = null;
		Tree.Exp fp = new Tree.TEMP(frame.FP());
		while(tLevel != target){
			ans = tLevel.staticLink().acc.exp(fp);
			tLevel = tLevel.parent;
			fp = ans;
		}
		return ans;
    }
}