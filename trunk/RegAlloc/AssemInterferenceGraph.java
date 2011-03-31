//Might be useless

package RegAlloc;
import Graph.Node;
import Graph.Graph;
import Temp.TempList;
public class AssemInterferenceGraph extends InterferenceGraph {
	public AssemInterferenceGraph(Assem.InstrList instrs){
		while(instrs != null){
			TempList def = instrs.head.def();
			while(def != null){
				if(tnode(def.head) == null)
					addBind(newNode(), def.head);
				def = def.tail;
			}
			TempList use = instrs.head.use();
			while(use != null){
				if(tnode(use.head) == null)
					addBind(newNode(), use.head);
				use = use.tail;
			}
			instrs = instrs.tail;
		}
	}
	public MoveList moves(){
		return null;
	}

	public int spillCost(Node node) {return 1;}
}

