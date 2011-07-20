package types;

public class RECORD extends Type {
	public symbol.Symbol fieldName;
	public Type fieldType;
	public RECORD tail;
	public RECORD(symbol.Symbol n, Type t, RECORD x) {
		fieldName=n; fieldType=t; tail=x;
	}
	public boolean coerceTo(Type t) {
		return this==t.actual();
	}
	public Type check(symbol.Symbol n){
		if(fieldName == n)return fieldType;
		if(tail != null)return tail.check(n);
		return null;
	}
}
   

