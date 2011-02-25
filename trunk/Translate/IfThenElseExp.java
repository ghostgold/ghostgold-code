package Translate;
import Temp.Label;

class IfThenElseExp extends Exp{
	Exp cond;
	Exp a;
	Exp b;
	Label t = new Label();
	Label f = new Label();
	Label join = new Label();
	IfThenElseExp(Exp cc, Exp aa, Exp bb){
		cond = cc;
		a = aa;
		b = bb;
	}
	public Tree.Stm unCx(Label iftrue, Label iffalse){
		if(b != null)
		return new Tree.SEQ(cond.unCx(t,f),
				new Tree.SEQ(new Tree.LABEL(t), 
				 new Tree.SEQ(a.unCx(iftrue,iffalse), 
				  new Tree.SEQ(new Tree.JUMP(join),
				   new Tree.SEQ(new Tree.LABEL(f), 
					new Tree.SEQ(b.unCx(iftrue, iffalse),
					 new Tree.LABEL(join)))))));
		else throw new Error("if with out else cannot be condition");
	}
	public Tree.Stm unNx(){
		if(b != null)
		return new Tree.SEQ(cond.unCx(t,f),
				new Tree.SEQ(new Tree.LABEL(t), 
				 new Tree.SEQ(a.unNx(), 
				  new Tree.SEQ(new Tree.JUMP(join),
				   new Tree.SEQ(new Tree.LABEL(f), 
					new Tree.SEQ(b.unNx(),
					 new Tree.LABEL(join)))))));
		else 
		return new Tree.SEQ(cond.unCx(t,f),
				new Tree.SEQ(new Tree.LABEL(t), 
				 new Tree.SEQ(a.unNx(), 
				   new Tree.LABEL(f))));

	}
	public Tree.Exp unEx(){
		Temp.Temp r = new Temp.Temp();
		if(b != null)
		return new Tree.ESEQ(new Tree.SEQ(cond.unCx(t,f),
						      new Tree.SEQ(new Tree.LABEL(t),
							   new Tree.SEQ(new Tree.MOVE(new Tree.TEMP(r), a.unEx()),
								new Tree.SEQ(new Tree.JUMP(join),
							 	 new Tree.SEQ(new Tree.LABEL(f),
								  new Tree.SEQ(new Tree.MOVE(new Tree.TEMP(r), b.unEx()),
								   new Tree.LABEL(join))))))),
							 new Tree.TEMP(r));
		else throw new Error("if must have else to produce a Ex");
	}
}