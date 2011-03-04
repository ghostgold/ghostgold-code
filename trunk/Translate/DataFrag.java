package Translate;
class DataFrag extends Frag{
	String data;
	Temp.Label label;
	DataFrag(String value, Frag next){
		super(next);
		data = value;
		label = new Temp.Label();
	}
}