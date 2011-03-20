package Temp;

public class Temp  {
   private static int count;
   public int num;
	private String name = "t";
	public String toString() {if(name.equals("t"))return "t" + num;else return name;}
   public Temp() { 
     num=count++;
   }
	public Temp(String n){
		name = n;
	   num = count++;
	   
	}
}

