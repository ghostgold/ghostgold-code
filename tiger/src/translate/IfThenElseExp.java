package translate;
import temp.AtomicLabel;

class IfThenElseExp extends Exp{
	Exp cond;
	Exp a;
	Exp b;
	AtomicLabel t = new AtomicLabel();
	AtomicLabel f = new AtomicLabel();
	AtomicLabel join = new AtomicLabel();
	IfThenElseExp(Exp cc, Exp aa, Exp bb){
		cond = cc;
		a = aa;
		b = bb;
	}
	public tree.Stm unCx(AtomicLabel iftrue, AtomicLabel iffalse){
		if(b != null)
		return new tree.Seq(cond.unCx(t,f),
				new tree.Seq(new tree.Label(t), 
				 new tree.Seq(a.unCx(iftrue,iffalse), 
				  new tree.Seq(new tree.Jump(join),
				   new tree.Seq(new tree.Label(f), 
					new tree.Seq(b.unCx(iftrue, iffalse),
					 new tree.Label(join)))))));
		else throw new Error("if with out else cannot be condition");
	}
	public tree.Stm unNx(){
		if(b != null)
		return new tree.Seq(cond.unCx(t,f),
				new tree.Seq(new tree.Label(t), 
				 new tree.Seq(a.unNx(), 
				  new tree.Seq(new tree.Jump(join),
				   new tree.Seq(new tree.Label(f), 
					new tree.Seq(b.unNx(),
					 new tree.Label(join)))))));
		else 
		return new tree.Seq(cond.unCx(t,f),
				new tree.Seq(new tree.Label(t), 
				 new tree.Seq(a.unNx(), 
				   new tree.Label(f))));

	}
	public tree.Exp unEx(){
		temp.AtomicTemp r = new temp.AtomicTemp();
		if(b != null)
		return new tree.Eseq(new tree.Seq(cond.unCx(t,f),
						      new tree.Seq(new tree.Label(t),
							   new tree.Seq(new tree.Move(new tree.Temp(r), a.unEx()),
								new tree.Seq(new tree.Jump(join),
							 	 new tree.Seq(new tree.Label(f),
								  new tree.Seq(new tree.Move(new tree.Temp(r), b.unEx()),
								   new tree.Label(join))))))),
							 new tree.Temp(r));
		else throw new Error("if must have else to produce a Ex");
	}
}