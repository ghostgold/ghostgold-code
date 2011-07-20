package Translate;
/** translate.Level */

import Util.BoolList;
public class Level {
    /** the frame of current level */
    Frame.Frame         frame;
    /** parent level */
    public Level        parent;
	
	public Level(Level p, Temp.Label n, BoolList f, int out){
		parent = p;
		frame = p.frame.newFrame(n, new BoolList(true, f), out);
	}
	
	public Level(Frame.Frame f){
		frame = f;
	}
	/**
     * allocate a new variable of the level
     * @param escape whether the variable has to be stored in memory
     * @return the translate.Access representation of the variable
     */
    public Access allocLocal( boolean escape ){
		return new Access(this, frame.allocLocal(escape));
    }
	/** return parameter list without static link*/
	public AccessList getFormals(){

		Frame.AccessList frameFormals = frame.getFormals();
		if(frameFormals.tail != null)return new AccessList(this, frameFormals.tail);
		else return null;
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
		Level tLevel = this;
		Tree.Exp fp = new Tree.TEMP(frame.FP());
		if(tLevel == target)return fp;

		tLevel = this.parent;
		fp = new Tree.TEMP(frame.FFP());
		if(tLevel == target)return fp;
		Tree.Exp ans = fp;
		while(tLevel != target){
			ans = tLevel.staticLink().exp(fp);
			tLevel = tLevel.parent;
			fp = ans;
		}
		return ans;
		/*
		Level tLevel = this;
		Tree.Exp fp = new Tree.TEMP(frame.FP());
		Tree.Exp ans = fp;
		while(tLevel != target){
			ans = tLevel.staticLink().exp(fp);
			tLevel = tLevel.parent;
			fp = ans;
		}
		return ans;*/
		
    }
}