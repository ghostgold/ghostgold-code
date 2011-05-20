package FlowGraph;
import Graph.*;
import Assem.*;
public class AssemFlowGraph extends FlowGraph {
	public java.util.Dictionary<Node,Instr> nodeToInstr = new java.util.Hashtable();
	public java.util.Dictionary labelToNode = new java.util.Hashtable();
	public Instr instr(Node n){
		return (Instr)nodeToInstr.get(n);
	}
	public AssemFlowGraph(InstrList instrs){
		InstrList saveInstr = instrs;
		Node prenode = newNode();
		nodeToInstr.put(prenode, instrs.head);
		if(instrs.head instanceof LABEL)
			labelToNode.put(((LABEL)instrs.head).label, prenode);
		instrs = instrs.tail;
		while(instrs != null){
			Node node = newNode();
			nodeToInstr.put(node, instrs.head);
			if(instrs.head instanceof LABEL)
				labelToNode.put(((LABEL)instrs.head).label, node);
			if(!nodeToInstr.get(prenode).assem.equals("j `j0"))addEdge(prenode, node);
			prenode = node;
			instrs = instrs.tail;
		}
		NodeList nodeList = nodes();
		while(nodeList != null){
			Instr instr = (Instr)nodeToInstr.get(nodeList.head);
			Targets targetsLabels = instr.jumps();
			
			if(targetsLabels != null && !instr.assem.equals("jal `j0 ")){
				Temp.LabelList labels = targetsLabels.labels;
				while(labels != null){
					Node targetNode = (Node)labelToNode.get(labels.head);
					addEdge(nodeList.head, targetNode);
					labels = labels.tail;
				}
			}
			nodeList = nodeList.tail;
		}
	}
	public Temp.TempList def(Node node){
		return instr(node).def();
	}
	public Temp.TempList use(Node node){
		return instr(node).use();
	}
	public boolean isMove(Node node){
		return (instr(node) instanceof MOVE);
	}
}