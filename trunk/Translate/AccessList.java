package Translate;
public class AccessList{
	public Access head;
	public AccessList tail;
	public AccessList(Access h, AccessList t){
		head = h;
		tail = t;
	}
	public AccessList(Level l, Frame.AccessList acc){
		head = new Access(l, acc.head);
		if(acc.tail != null)tail = new AccessList(l, acc.tail);
		else tail = null;

	}
}