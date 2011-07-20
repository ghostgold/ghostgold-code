package temp;

public class AtomicTemp  {
   private static int count;
	public int num;
	public int precolor;
	public int spillcost;
	private String name = "t";
	public String toString() {if(!this.precolored())return "t" + num;else return name;}
	public AtomicTemp() {
		precolor = -1;
		num=count++;
		spillcost = 0;
	}
	public AtomicTemp(String n, int pre){
		precolor = pre;
		name = n;
		num = count++;
	}
	public boolean precolored(){
		return  (precolor>=0);
	}
	public void addSpillCost(int cost){
		if(precolored())return;
		if(spillcost < 0)return;
		spillcost+=cost;
	}
	public void setSpillCost(int cost){
		spillcost = cost;
	}
	public int  spillCost(){
		if(precolored())return Integer.MAX_VALUE;
		if(spillcost < 0)return Integer.MAX_VALUE;
		return spillcost;
	}
}

