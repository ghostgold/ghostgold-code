package Semant;
public class FunEntry extends Entry
{
	public Translate.Level level;
	public Temp.Label label;
	public Types.RECORD formals;
	public Types.Type result;
	public FunEntry(Translate.Level v, Temp.Label l, Types.RECORD f,Types.Type r){
		this(f,r);
		level = v;
		label = l;
	}
	public FunEntry(Types.RECORD f, Types.Type r){
		formals = f;
		result = r;
	}
}