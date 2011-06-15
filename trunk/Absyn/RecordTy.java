package Absyn;
import Symbol.Symbol;
public class RecordTy extends Ty {
   public FieldList fields;
   public RecordTy(int p, FieldList f) {pos=p; fields=f;}
	public RecordTy clone(){
		if(fields != null)return new RecordTy(pos, fields.clone());
		else return new RecordTy(pos, null);
	}
}   
