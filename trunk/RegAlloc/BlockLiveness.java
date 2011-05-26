package RegAlloc;
import Graph.*;
import FlowGraph.*;
import Temp.*;
import java.util.*;
public class BlockLiveness {
	java.util.Dictionary<Node, HashSet<Temp>> liveIn = new java.util.Hashtable();
	java.util.Dictionary<Node, HashSet<Temp>> liveOut = new java.util.Hashtable();
	java.util.Dictionary<Assem.BasicBlock, HashSet<Temp>> liveMap = new java.util.Hashtable();
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
				HashSet<Temp> in = liveIn.get(nodes.head);
				HashSet<Temp> out = liveOut.get(nodes.head);
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
					HashSet<Temp> succin = liveIn.get(succ.head);
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
	public Set<Temp> liveAt(Assem.BasicBlock i){
		return liveMap.get(i);
	}
}


