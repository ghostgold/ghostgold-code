package Temp;

public class Temp  {
   private static int count;
	public int num;
	boolean pre;
	private String name = "t";
	public String toString() {if(name.equals("t"))return "t" + num;else return name;}
	public Temp() {
		pre = false;
		num=count++;
	}
	public Temp(String n){
		pre = true;
		name = n;
		num = count++;
	   
	}
	public boolean precolored(){
		return  pre;
	}
}

