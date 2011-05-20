package Translate;
public class DataFrag extends Frag{
	public String data;
	public Temp.Label label;
	DataFrag(String value, Frag next){
		super(next);
		data = value;
		label = new Temp.Label();
	}
}