package Semant;
class VarEntry extends Entry
{
	Translate.Access access;
	Types.Type ty;
	public VarEntry(Translate.Access a, Types.Type t){
		this(t);
		access = a;
	}
	public VarEntry(Types.Type t){
		ty = t;
	}
}