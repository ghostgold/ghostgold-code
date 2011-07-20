package regalloc;
import java.util.*;

import temp.*;

import flowgraph.*;
import graph.*;
public class BlockLiveness {
	java.util.Dictionary<Node, HashSet<AtomicTemp>> liveIn = new java.util.Hashtable();
	java.util.Dictionary<Node, HashSet<AtomicTemp>> liveOut = new java.util.Hashtable();
	java.util.Dictionary<assem.BasicBlock, HashSet<AtomicTemp>> liveMap = new java.util.Hashtable();
	public BlockLiveness(BlockFlowGraph flow){
		boolean change = true;
		//		flow.show(System.out);
		for(NodeList nodes = flow.nodes(); nodes != null; nodes = nodes.tail){
			liveIn.put(nodes.head, new HashSet());
			liveOut.put(nodes.head, new HashSet());
		}
		while(change){
			change = false;
			NodeList nodes = flow.nodes();
			while(nodes != null){
				HashSet<AtomicTemp> in = liveIn.get(nodes.head);
				HashSet<AtomicTemp> out = liveOut.get(nodes.head);
				int inp = in.size();
				int outp = out.size();
				TempList use = flow.use(nodes.head);
				TempList def = flow.def(nodes.head);
				in = (HashSet)out.clone();
				while(def != null){
					in.remove(def.head);
					def = def.tail;
				}
				while(use != null){
					in.add(use.head);
					use = use.tail;
				}
				out = new HashSet();
				NodeList succ = nodes.head.succ();
				while(succ != null){
					HashSet<AtomicTemp> succin = liveIn.get(succ.head);
					out.addAll(succin);
					succ = succ.tail;
				} 
				if(inp < in.size()){
					change = true;
					liveIn.put(nodes.head, in);
				}

				if(outp < out.size()){
					change = true;
					liveOut.put(nodes.head, out);
				}
				nodes = nodes.tail;
			}
		}
		NodeList nodes = flow.nodes();
		while(nodes != null){
			//			Iterator<Temp> it = liveOut.get(nodes.head).iterator();
			//			TempList liveat = null;
			//			while(it.hasNext())
			//				liveat = new TempList(it.next(), liveat);
			liveMap.put(flow.instr(nodes.head), liveOut.get(nodes.head));
			nodes = nodes.tail;
		}
	}
	public Set<AtomicTemp> liveAt(assem.BasicBlock i){
		return liveMap.get(i);
	}
}


