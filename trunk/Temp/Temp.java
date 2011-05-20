package Temp;

public class Temp  {
   private static int count;
	public int num;
	public int precolor;
	private String name = "t";
	public String toString() {if(!this.precolored())return "t" + num;else return name;}
	public Temp() {
		precolor = -1;
		num=count++;
	}
	public Temp(String n, int pre){
		precolor = pre;
		name = n;
		num = count++;
	}
	public boolean precolored(){
		return  (precolor>=0);
	}
}

