package FlowGraph;
import Graph.*;
import Assem.*;
import java.util.*;
public class BlockFlowGraph extends FlowGraph {
	public java.util.Dictionary<Node,BasicBlock> nodeToInstr = new Hashtable();
	public java.util.Dictionary<Temp.Label, Node> labelToNode = new Hashtable();
	public java.util.Dictionary<BasicBlock, Node> instrToNode = new Hashtable();
	public BasicBlock instr(Node n){
		return (BasicBlock)nodeToInstr.get(n);
	}
	public Node getNodeOfInstr(BasicBlock bb){
		return instrToNode.get(bb);
	}
	public Node entry;
	public BlockFlowGraph(ArrayList<BasicBlock> blocks){
		ListIterator<BasicBlock> it = blocks.listIterator();
		Node node = null;
		Node prenode = null;
		while(it.hasNext()){
			prenode = node;
			BasicBlock block = it.next();
			node = newNode();
			nodeToInstr.put(node, block);
			instrToNode.put(block, node);
			if(block.label != null)
				labelToNode.put(block.label, node);
			if(prenode != null && !(instr(prenode).last instanceof JUMP))addEdge(prenode, node);
			if(prenode == null)
				entry = node;
		}

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
				/*
				  Jump zipping
				  if(lastOfBlock instanceof JUMP) {
					Instr preSecondInstr = lastOfBlock;
					Instr secondInstr = new BINOP("addi",null, null, null, 0, -1);
					Temp.Label dstLabel = targetsLabels.labels.head;
					Node dstNode = labelToNode.get(dstLabel);
					BasicBlock dstBlock = nodeToInstr.get(dstNode);
					if(dstBlock.instrs.tail != null)
						secondInstr = dstBlock.instrs.tail.head;
					while(secondInstr instanceof JUMP){
						dstLabel = secondInstr.jumps().labels.head;
						dstNode = labelToNode.get(dstLabel);
						dstBlock = nodeToInstr.get(dstNode);
						preSecondInstr = secondInstr;
						if(dstBlock.instrs.tail != null)
							secondInstr = dstBlock.instrs.tail.head;
						else break;
					}
					((OPER)lastOfBlock).jump = preSecondInstr.jumps();
					addEdge(nodeList.head, labelToNode.get(preSecondInstr.jumps().labels.head));
					}*/
				//				else {
						//	}
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