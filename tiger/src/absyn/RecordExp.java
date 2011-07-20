package absyn;
import symbol.Symbol;
public class RecordExp extends Exp {
   public Symbol typ;
   public FieldExpList fields;
   public RecordExp(int p, Symbol t, FieldExpList f) {pos=p; typ=t;fields=f;}
	public RecordExp clone(){
		if(fields != null)return new RecordExp(pos, typ, fields.clone());
		else return new RecordExp(pos, typ, null);
	}
}
