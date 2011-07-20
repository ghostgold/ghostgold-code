package translate;
public class DataFrag extends Frag{
	public String data;
	public temp.AtomicLabel label;
	DataFrag(String value, Frag next){
		super(next);
		data = value;
		label = new temp.AtomicLabel();
	}
}