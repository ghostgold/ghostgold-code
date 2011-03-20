package RegAlloc;
import Graph.*;
import FlowGraph.*;
import Temp.TempList;
public class Liveness extends InterferenceGraph{
	java.util.Dictionary liveIn = new java.util.Hashtable();
	java.util.Dictionary liveOut = new java.util.Hashtable();
	public Liveness(FlowGraph flow){
		//		NodeList nodes = flow.nodes;
		boolean change = true;
		while(change){
			change = false;
			NodeList nodes = flow.nodes();
			while(nodes != null){
				TempList in = (TempList)liveIn.get(nodes.head); //ordered
				TempList out = (TempList)liveOut.get(nodes.head); // ordered
				TempList inp;
				TempList outp;
				if(in != null)inp = in.clone(); //in'
				if(out != null)outp = out.clone(); //out'
				TempList use = flow.use(nodes.head);//not ordered
				TempList def = flow.def(nodes.head);//not ordered
				if(out != null)in = out.clone();
				while(def != null){
					in.remove(def.head);
					def = def.tail;
				}
				while(use != null){
					in.add(use.head);
					use = use.tail;
				}
				out = null;
				NodeList succ = nodes.head.succ();
				while(succ != null){
					if(out == null)out = ((TempList)liveIn.get(succ.head)).clone();
					else out = out.union((TempList)liveIn.get(succ.head));
					succ = succ.tail;
				}
				if(inp == null && in != null)change = true;
				else if(!inp.same(in))
					change = true;
				if(outp == null && out != null)change = true;
				else if(!outp.same(out))change = true;
				nodes = nodes.tail;
			}
		}
	}
}