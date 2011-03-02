package Semant;
public class ExpTyList{
	public ExpTy head;
	public ExpTyList tail;
	public ExpTyList(ExpTy h, ExpTyList t){
		head = h;
		tail = t;
	}
}