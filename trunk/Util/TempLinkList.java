package Util;
public class TempLinkList {
	java.util.LinkedList temps = new java.util.LinkedList<Temp.Temp>;
	void union(TempLinksList another){
		java.util.LinkedList other = another.temps;
		java.util.ListIterator<Temp.Temp> it = temps.listIterator(0);
		java.util.ListIterator<Temp.Temp> oit = other.listIterator(0);
		while(oit.hasNext()){
			if(it.hasNext()){
				if(oit.next().num == it.next().num)

			}
		}
	}
	
}