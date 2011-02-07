package Types;

public class RECORD extends Type {
	public Symbol.Symbol fieldName;
	public Type fieldType;
	public RECORD tail;
	public RECORD(Symbol.Symbol n, Type t, RECORD x) {
		fieldName=n; fieldType=t; tail=x;
	}
	public boolean coerceTo(Type t) {
		return this==t.actual();
	}
	public Type check(Symbol.Symbol n){
		if(fieldName == n)return fieldType;
		if(tail != null)return tail.check(n);
		return null;
	}
}
   

