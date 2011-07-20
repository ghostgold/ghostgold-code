package regalloc;
import graph.*;

import java.util.*;
class SpillComparator implements Comparator<Node>
{
	InterferenceGraph graph;
	SpillComparator(InterferenceGraph g){
		graph = g;
	}
	public int compare(Node a, Node b){
		temp.AtomicTemp  x = graph.gtemp(a);
		temp.AtomicTemp y = graph.gtemp(b);
		if(x.spillCost() < y.spillCost())return -1;
		if(x.spillCost() > y.spillCost())return 1;
		return 0;
	}
	public boolean equals(Object o){
		return (this == o);
	}
}