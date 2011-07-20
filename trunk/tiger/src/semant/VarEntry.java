package semant;
class VarEntry extends Entry
{
	translate.Access access;
	types.Type ty;
	public VarEntry(translate.Access a, types.Type t){
		this(t);
		access = a;
	}
	public VarEntry(types.Type t){
		ty = t;
	}
}