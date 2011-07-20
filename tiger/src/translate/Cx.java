package translate;
import temp.AtomicLabel;
import temp.AtomicTemp;
abstract class Cx extends Exp{
	public tree.Exp unEx(){
		AtomicTemp r = new AtomicTemp();
		AtomicLabel t = new AtomicLabel();
		AtomicLabel f = new AtomicLabel();
		
		return new tree.Eseq(
							 new tree.Seq(new tree.Move(new tree.Temp(r),
														new tree.Const(1)),
										  new tree.Seq(unCx(t,f),
											  new tree.Seq(new tree.Label(f),
												  new tree.Seq(new tree.Move(new tree.Temp(r),
													  new tree.Const(0)),
														  new tree.Label(t))))),
							 new tree.Temp(r));
		
	}
	public tree.Stm unNx(){
		AtomicLabel t = new AtomicLabel();
		return new tree.Seq(unCx(t,t), new tree.Label(t));
	}
	
	public abstract tree.Stm unCx(AtomicLabel t, AtomicLabel f);
}