package RegAlloc;
import java.util.*;
import Graph.*;
class SpillComparator implements Comparator<Node>
{
	InterferenceGraph graph;
	SpillComparator(InterferenceGraph g){
		graph = g;
	}
	public int compare(Node a, Node b){
		Temp.Temp  x = graph.gtemp(a);
		Temp.Temp y = graph.gtemp(b);
		if(x.spillCost() < y.spillCost())return -1;
		if(x.spillCost() > y.spillCost())return 1;
		return 0;
	}
	public boolean equals(Object o){
		return (this == o);
	}
}