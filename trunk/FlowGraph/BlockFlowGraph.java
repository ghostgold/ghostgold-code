package FlowGraph;
import Graph.*;
import Assem.*;
import java.util.*;
public class BlockFlowGraph extends FlowGraph {
	public java.util.Dictionary<Node,BasicBlock> nodeToInstr = new Hashtable();
	public java.util.Dictionary<Temp.Label, Node> labelToNode = new Hashtable();
	public BasicBlock instr(Node n){
		return (BasicBlock)nodeToInstr.get(n);
	}
	public BlockFlowGraph(ArrayList<BasicBlock> blocks){
		ListIterator<BasicBlock> it = blocks.listIterator();
		Node node = null;
		Node prenode = null;
		while(it.hasNext()){
			prenode = node;
			BasicBlock block = it.next();
			node = newNode();
			nodeToInstr.put(node, block);
			if(block.label != null)
				labelToNode.put(block.label, node);
			if(prenode != null && !(instr(prenode).last instanceof JUMP))addEdge(prenode, node);
		}
		// if(instrs.head instanceof LABEL)
		// 	labelToNode.put(((LABEL)instrs.head).label, prenode);
		// instrs = instrs.tail;
		// while(instrs != null){
		// 	Node node = newNode();
		// 	nodeToInstr.put(node, instrs.head);
		// 	if(instrs.head instanceof LABEL)
		// 		labelToNode.put(((LABEL)instrs.head).label, node);
		// 	if(!nodeToInstr.get(prenode).assem.equals("j `j0"))addEdge(prenode, node);
		// 	prenode = node;
		// 	instrs = instrs.tail;
		// }
		NodeList nodeList = nodes();
		while(nodeList != null){
			BasicBlock instr = nodeToInstr.get(nodeList.head);
			Instr lastOfBlock = instr.last;
			Targets targetsLabels = lastOfBlock.jumps();
			
			if(targetsLabels != null && !(lastOfBlock instanceof CALL)){
				Temp.LabelList labels = targetsLabels.labels;
				while(labels != null){
					Node targetNode = labelToNode.get(labels.head);
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
	public Temp.TempList tot(Node node){
		return instr(node).tot();
	}
	public boolean isMove(Node node){
		return false;
	}
}