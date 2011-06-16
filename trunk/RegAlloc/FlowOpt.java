package RegAlloc;
import Assem.*;
import FlowGraph.BlockFlowGraph;
import java.util.*;
import Temp.*;
import Graph.*;
public class FlowOpt
{
	static boolean change;
	public static void flowOpt(ArrayList<BasicBlock> instr){
		change = true;
		while(change){
			change = false;
			copyPropgation(instr);
			//subexpressionElimination(instr);
			deadCodeElimination(instr);
		}

	}
	public static void subexpressionElimination(ArrayList<BasicBlock> instr){
		BlockFlowGraph flow = new BlockFlowGraph(instr);
		
	}
	public static void deadCodeElimination(ArrayList<BasicBlock> instrs){
		BlockFlowGraph flow = new BlockFlowGraph(instrs);
		BlockLiveness liveness = new BlockLiveness(flow);
		for(BasicBlock block : instrs){
			ArrayList<Instr> reverseIns = block.toArrayListReverse();
			Set<Temp> live = liveness.liveAt(block);
			for(Instr i : reverseIns){
				i.dead = false;
			}
			for(Instr i: reverseIns){
				if(i instanceof BINOP){
					BINOP binop = (BINOP)i;
					if(!live.contains(binop.dst)){
						i.dead = true;
						change = true;
					}
					else {
						for(TempList def = i.def(); def != null ; def = def.tail)
							live.remove(def.head);
						for(TempList use = i.use(); use != null ; use = use.tail)
							live.add(use.head);
					}
				}
				else if (i instanceof MEM && i.opcode == MEM.LW){
					MEM mem = (MEM)i;
					if(!live.contains(mem.dst)){
						i.dead = true;
						change = true;
					}
					else {
						for(TempList def = i.def(); def != null ; def = def.tail)
							live.remove(def.head);
						for(TempList use = i.use(); use != null ; use = use.tail)
							live.add(use.head);
					}
				}
				else {
					for(TempList def = i.def(); def != null ; def = def.tail)
						live.remove(def.head);
					for(TempList use = i.use(); use != null ; use = use.tail)
						live.add(use.head);
				}
			}
			InstrList newInstr = null;
			boolean deadEliminated = false;
			for(Instr i: reverseIns){
				if(!i.dead)
					newInstr = new InstrList(i, newInstr);
				else 
					deadEliminated = true;
			}
			if(deadEliminated)
				block.setInstrs(newInstr);
		}
	}
	public static void copyPropgation(ArrayList<BasicBlock> instrs){
		LinkedHashSet<CopyPair> total = new LinkedHashSet();
		BlockFlowGraph flow = new BlockFlowGraph(instrs);
		Map<Node, LinkedHashSet<CopyPair>> copySet = new LinkedHashMap();
		Map<Node, LinkedHashSet<CopyPair>> cpIn = new LinkedHashMap();
		Map<Node, LinkedHashSet<CopyPair>> cpOut = new LinkedHashMap();

		for(NodeList nodes = flow.nodes(); nodes != null; nodes = nodes.tail){
			LinkedHashSet<CopyPair> acp = copyLocalPropgation(flow.instr(nodes.head), new LinkedHashSet());
			copySet.put(nodes.head, acp);
			total.addAll(acp);
		}

		for(NodeList nodes = flow.nodes(); nodes != null; nodes = nodes.tail){
			cpIn.put(nodes.head, (LinkedHashSet)total.clone());
			cpOut.put(nodes.head, (LinkedHashSet)total.clone());
		}

		cpIn.put(flow.entry, new LinkedHashSet());
		boolean flowChange = true;
		while(flowChange){
			flowChange = false;
			for(BasicBlock block : instrs){
				//node is what to be done with in and out
				Node node = flow.getNodeOfInstr(block);
				NodeList pred = node.pred();
				LinkedHashSet<CopyPair> oldIn = cpIn.get(node);
				LinkedHashSet<CopyPair> newIn;
				if(pred != null){
					newIn = new LinkedHashSet(cpOut.get(pred.head));
					for(NodeList p = pred.tail; p != null; p = p.tail){
						newIn.retainAll(cpOut.get(p.head));
					}
				}
				else newIn = new LinkedHashSet();
				if(!newIn.equals(oldIn)){
					/*					System.out.println(block.label.toString() + "in");
					for(CopyPair copy : newIn){
						System.out.println(copy.to.toString() + "<="+  copy.from.toString());
						}*/
					flowChange = true;
					cpIn.put(node, newIn);
				}
				//CPout = CPin
				LinkedHashSet<CopyPair> newOut = new LinkedHashSet(newIn);
				InstrList ins = block.instrs;
				//CPout = CPin - KILL(i)
				while(ins != null){
					if(ins.head instanceof OPER){
						OPER oper = (OPER)ins.head;
						if(oper.dst != null)
							removeACP(oper.dst, newOut);
					}
					else if(ins.head instanceof MOVE){
						MOVE move = (MOVE)ins.head;
						removeACP(move.dst, newOut);
					}
					else if(ins.head instanceof CALL){
						CALL call =  (CALL)ins.head;
						for(TempList dsts = call.def(); dsts != null; dsts = dsts.tail)
							removeACP(dsts.head, newOut);
					}
					ins = ins.tail;
				}
				//CPout += COPY(i)
				newOut.addAll(copySet.get(node));
				if(!newOut.equals(cpOut.get(node))){
					/*					System.out.println(block.label.toString() + "out");
					for(CopyPair copy : newIn){
						System.out.println(copy.to.toString() + "<=" +copy.from.toString());
						}*/
					cpOut.put(node, newOut);
					flowChange = true;
				}
			}
		}

		for(BasicBlock block: instrs){
			copyLocalPropgation(block, new LinkedHashSet(cpIn.get(flow.getNodeOfInstr(block))));
		}
		
		
	}
	public static LinkedHashSet<CopyPair> copyLocalPropgation(BasicBlock block, LinkedHashSet<CopyPair> acp){
		InstrList instrs = block.instrs;
		//what is in Assem BRANCH JUMP MEM BINOP CALL MOVE
		//what will def regs? MEM BINOP CALL MOVE(kill copy)
		//what will use regs? BRANCH MEM BINOP CALL* MOVE(prop to)
		//do not propgation to CALL
		//what is OPER BRANCH BINOP JUMP MEM
		while(instrs != null){
			if(instrs.head instanceof OPER){
				OPER oper = (OPER)instrs.head;
				oper.setSrc(copyValue(oper.left, acp), copyValue(oper.right, acp));
				if(oper.dst != null)
					removeACP(oper.dst, acp);
			}
			else if(instrs.head instanceof MOVE){
				MOVE move = (MOVE)instrs.head;
				//	move.src = copyValue(move.src, acp);
				removeACP(move.dst, acp);
				if(move.dst != move.src)
					acp.add(new CopyPair(move));
			}
			else if(instrs.head instanceof CALL){
				CALL call =  (CALL)instrs.head;
				for(TempList dsts = call.def(); dsts != null; dsts = dsts.tail)
					removeACP(dsts.head, acp);
			}
			instrs = instrs.tail;
		}
		block.setInstrs(block.instrs);
		return acp;
	}
	public static void removeACP(Temp t, LinkedHashSet<CopyPair> acp){
		List<CopyPair> del = new ArrayList();
		for(CopyPair copy : acp){
			if(t == copy.from || t == copy.to)
				del.add(copy);
		}
		for(CopyPair copy : del){
			acp.remove(copy);
		}
	}
	public static Temp copyValue(Temp t, LinkedHashSet<CopyPair> acp){
		if(t == null)return null;
		for(CopyPair copy: acp){
			if(copy.to == t){
				change = true;
				return copy.from;
			}
		}
		return t;
	}
}