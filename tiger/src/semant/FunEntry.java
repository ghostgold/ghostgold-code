package semant;
public class FunEntry extends Entry
{
	public translate.Level level;
	public temp.AtomicLabel label;
	public types.RECORD formals;
	public types.Type result;
	public FunEntry(translate.Level v, temp.AtomicLabel l, types.RECORD f,types.Type r){
		this(f,r);
		level = v;
		label = l;
	}
	public FunEntry(types.RECORD f, types.Type r){
		formals = f;
		result = r;
	}
}