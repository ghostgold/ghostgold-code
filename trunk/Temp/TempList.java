package Temp;

public class TempList {
	public Temp head;
	public TempList tail;
	public int size;
	public TempList(Temp h, TempList t) {
		head=h; tail=t;
		if(tail == null)
			size = 1;
		else size = tail.size + 1;
	}
	public TempList(Temp h){
		head = h;
		tail = null;
	}
	public TempList union(TempList b){
		if(b == null)return this;
		TempList ans = new TempList(null, null);
		TempList saveans = ans;
		TempList a = this;
		while(a != null || b != null){
			if(a == null){
				ans.tail = b.clone();
				break;
			}
			if(b == null){
				ans.tail = a.clone();
				break;
			}
			if(a.head.num < b.head.num){
				ans.tail = new TempList(a.head, null);
				ans = ans.tail;
				a = a.tail;
			}
			else if(a.head.num > b.head.num){
				ans.tail = new TempList(b.head, null);
				ans = ans.tail;
				b = b.tail;
			}
			else if(a.head.num == b.head.num){
				ans.tail = new TempList(a.head, null);
				ans = ans.tail;
				a = a.tail;
				b = b.tail;
			}
		}
		return saveans.tail;
	}
	public TempList cloneAdd(Temp t){
		if(t.num == head.num)return this.clone();
		else if(t.num > head.num){
			if(tail != null)
				return new TempList(head, tail.cloneAdd(t));
			else
				return new TempList(head, new TempList(t, null));
		}
		else return new TempList(t, this.clone());
	}
	public TempList cloneRemove(Temp t){
		if(t.num == head.num){
			if(tail != null)
				return tail.clone();
			else 
				return null;
		}
		else if(t.num > head.num){
			if(tail != null)
				return new TempList(head, tail.cloneRemove(t));
			else return this;
		}
		else return this.clone();
	}
	public TempList add(Temp t){
		TempList a = this;
		TempList pre = null;
		while(a != null && a.head.num < t.num ){
			pre = a;
			a = a.tail;
		}
		if(a != null && a.head.num == t.num)return this;
		if(pre == null){
			tail = new TempList(head, tail);
			head = t;
		}
		else
			pre.tail = new TempList(t, a);
		return this;
	}
	public TempList remove(Temp t){
		TempList a = this;
		TempList pre = null;
		while(a != null && a.head.num < t.num){
			pre = a;
			a = a.tail;
		}
		if(a == null)return this;
		if(a.head.num == t.num){
			if(pre != null){
				pre.tail = a.tail;
				return this;
			}
			else {
				return a.tail;
			}
		}
		return this;
	}
	public TempList clone(){
		if(tail != null)
			return  new TempList(head, tail.clone());
		else
			return new TempList(head, null);
	}
	public boolean same(TempList t){
		return t.size == size;
	}
}

