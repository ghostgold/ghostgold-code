package RegAlloc;
import Assem.*;
import FlowGraph.BlockFlowGraph;
import java.util.*;
import Temp.*;
import Graph.*;
public class FlowOpt
{
	static boolean change;
	public static void deleteUnReachableCode(ArrayList<BasicBlock> instr){
		BlockFlowGraph flow = new BlockFlowGraph(instr);
		boolean codeChange = true;
		while(codeChange){
			codeChange =false;
			for(NodeList node = flow.nodes(); node != null; node = node.tail){
				if(node.head.pred() == null && node.head != flow.entry){
					if(instr.remove(flow.instr(node.head))){
						for(NodeList succ = node.head.succ(); succ != null; succ =succ.tail){
							flow.rmEdge(node.head, succ.head);
						}
						change = true;
					}
				}
			}
		}
		
	}
	public static void flowOpt(ArrayList<BasicBlock> instr, Frame.Frame frame){
		change = true;
		//		deleteUnReachableCode(instr);
		//	
		while(change){
			//			System.out.println(1);
			change = false;

			//			System.out.println(2);
			//			copyPropgation(instr);
			subexpressionElimination(instr, frame);		
			//			System.out.println(3);
			deadCodeElimination(instr);
			//			System.out.println(4);
		}
	}
	public static void subexpressionElimination(ArrayList<BasicBlock> instr, Frame.Frame frame){
		// BlockFlowGraph flow = new BlockFlowGraph(instr);
		// LinkedHashSet<String>  total = new LinkedHashSet();
		// Map<Node, Set<String>> evalSet = new LinkedHashMap();
		// Map<Node, Set<String>> availIn = new LinkedHashMap();
		// Map<Node, Set<String>> availOut = new LinkedHashMap();

		// for(NodeList nodes = flow.nodes(); nodes != null; nodes = nodes.tail){
		// 	Set<String> acp = subexpressionLocalElimination(flow.instr(nodes.head));
		// 	evalSet.put(nodes.head, new LinkedHashSet(acp));
		// 	total.addAll(acp);
		// }

		BlockFlowGraph flow = new BlockFlowGraph(instr);
		LinkedHashSet<InstrListTemp>  total = new LinkedHashSet();
		Map<Node, Set<InstrListTemp>> evalSet = new LinkedHashMap();
		Map<Node, Set<InstrListTemp>> availIn = new LinkedHashMap();
		Map<Node, Set<InstrListTemp>> availOut = new LinkedHashMap();

		for(NodeList nodes = flow.nodes(); nodes != null; nodes = nodes.tail){
			Set<InstrListTemp> acp = subexpressionLocalElimination(flow.instr(nodes.head));
			evalSet.put(nodes.head, new LinkedHashSet(acp));
			total.addAll(acp);
		}
		
		// for(NodeList nodes = flow.nodes(); nodes != null; nodes = nodes.tail){
		// 	availIn.put(nodes.head, (Set)total.clone());
		// 	availOut.put(nodes.head, (Set)total.clone());
		// }
		// boolean debug = false;
		// availIn.put(flow.entry, new LinkedHashSet());
		// boolean flowChange = true;
		// while(flowChange){
		// 	flowChange = false;
		// 	//			System.out.println("11");
		// 	for(BasicBlock block : instr){
		// 		//node is what to be done with in and out
		// 		Node node = flow.getNodeOfInstr(block);
		// 		NodeList pred = node.pred();
		// 		if(debug){
		// 			System.out.println(block.label+ "old in");
		// 			for(String s: availIn.get(node))
		// 				System.out.print(s+" ");
		// 			System.out.println();
		// 			System.out.println(block.label+ "old out");
		// 			for(String s: availOut.get(node))
		// 				System.out.print(s+" ");
		// 			System.out.println();
		// 		}
		// 		Set<String> oldIn = availIn.get(node);
		// 		Set<String> newIn ;
		// 		if(pred != null){
		// 			if(debug)System.out.println("pre " + flow.instr(pred.head).label.toString());
		// 			newIn = new LinkedHashSet(availOut.get(pred.head));
		// 			for(NodeList p = pred.tail; p != null; p = p.tail){
		// 				if(debug)System.out.println("pre " + flow.instr(p.head).label.toString());
		// 				newIn.retainAll(availOut.get(p.head));
		// 			}
		// 			if(!newIn.equals(oldIn)){
		// 				flowChange = true;
		// 				availIn.put(node, newIn);
		// 			}
		// 		}
		// 		else newIn = oldIn;

		// 		LinkedHashSet<String> newOut = new LinkedHashSet(newIn);
		// 		ArrayList<String> del = new ArrayList();
		// 		for(InstrList i = block.instrs; i != null; i = i.tail ){
		// 			for(TempList def = i.head.def(); def != null; def = def.tail){
		// 				for(String s : newOut){
		// 					if(s.indexOf("("+def.head.toString()+")") >= 0)del.add(s);
		// 				}
		// 			}
		// 			if(i.head.opcode == MEM.SW){
		// 				for(String s : newOut){
		// 					if(s.startsWith("lw") && !s.endsWith("nc") && aboutStack(s,frame) == ((MEM)i.head).aboutStack())
		// 						del.add(s);
		// 				}
		// 			}	
		// 			if(i.head instanceof CALL){
		// 				for(String s: newOut){
		// 					if(s.startsWith("lw") && !s.endsWith("nc")){
		// 						del.add(s);
		// 					}
		// 				}
		// 			}
		// 		}
		// 		for(String s : del)
		// 			newOut.remove(s);
		
		// 		newOut.addAll(evalSet.get(node));
		// 		if(!newOut.equals(availOut.get(node))){
		// 			availOut.put(node, newOut);
		// 			flowChange = true;
		// 		}
		// 		if(debug){
		// 			System.out.println(block.label+ "new in");
		// 			for(String s: availIn.get(node))
		// 				System.out.print(s+" ");
		// 			System.out.println();
		// 			System.out.println(block.label+ "new out");
		// 			for(String s: availOut.get(node))
		// 				System.out.print(s+" ");
		// 			System.out.println();
		// 		}
		// 	}
		// }
		// for(NodeList node = flow.nodes(); node != null; node = node.tail){
		// 	/*			System.out.println(flow.instr(node.head).label.toString());
		// 	for(String s: availIn.get(node.head))
		// 		System.out.print(s+" ");
		// 		System.out.println();*/
		// 	subexpressionLocalElimination(flow.instr(node.head), availIn.get(node.head), flow, frame);
		// }



		// for(NodeList nodes = flow.nodes(); nodes != null; nodes = nodes.tail){
		// 	availIn.put(nodes.head, (Set)total.clone());
		// 	availOut.put(nodes.head, (Set)total.clone());
		// }
		// boolean debug = false;
		// availIn.put(flow.entry, new LinkedHashSet());
		// boolean flowChange = true;
		// while(flowChange){
		// 	flowChange = false;
		// 	//			System.out.println("11");
		// 	for(BasicBlock block : instr){
		// 		//node is what to be done with in and out
		// 		Node node = flow.getNodeOfInstr(block);
		// 		NodeList pred = node.pred();
		// 		/*				if(debug){
		// 			System.out.println(block.label+ "old in");
		// 			for(String s: availIn.get(node))
		// 				System.out.print(s+" ");
		// 			System.out.println();
		// 			System.out.println(block.label+ "old out");
		// 			for(String s: availOut.get(node))
		// 				System.out.print(s+" ");
		// 			System.out.println();
		// 			}*/
		// 		Set<InstrListTemp> oldIn = availIn.get(node);
		// 		Set<InstrListTemp> newIn ;
		// 		if(pred != null){
		// 			//if(debug)System.out.println("pre " + flow.instr(pred.head).label.toString());
		// 			newIn = new LinkedHashSet(availOut.get(pred.head));
		// 			for(NodeList p = pred.tail; p != null; p = p.tail){
		// 				//if(debug)System.out.println("pre " + flow.instr(p.head).label.toString());
		// 				newIn.retainAll(availOut.get(p.head));
		// 			}
		// 			if(!newIn.equals(oldIn)){
		// 				flowChange = true;
		// 				availIn.put(node, newIn);
		// 			}
		// 		}
		// 		else newIn = oldIn;

		// 		LinkedHashSet<InstrListTemp> newOut = new LinkedHashSet(newIn);
		// 		ArrayList<InstrListTemp> del = new ArrayList();
		// 		for(InstrList i = block.instrs; i != null; i = i.tail ){
		// 			for(TempList def = i.head.def(); def != null; def = def.tail){
		// 				for(InstrListTemp s : newOut){
		// 					if(s.instr.head.toString().indexOf("("+def.head.toString()+")") >= 0)del.add(s);
		// 				}
		// 			}
		// 			if(i.head.opcode == MEM.SW){
		// 				for(InstrListTemp s : newOut){
		// 					String str = s.instr.head.toString();
		// 					if(str.startsWith("lw") &&
		// 					   !str.toString().endsWith("nc") && 
		// 					   aboutStack(str,frame) == ((MEM)i.head).aboutStack())
		// 						del.add(s);
		// 				}
		// 			}	
		// 			if(i.head instanceof CALL){
		// 				// for(InstrList s: newOut){
		// 				// 	if(s.startsWith("lw") && !s.endsWith("nc")){
		// 				// 		del.add(s);
		// 				// 	}
		// 				// }
		// 				newOut = new LinkedHashSet();
		// 			}
		// 		}
		// 		for(InstrListTemp s : del)
		// 			newOut.remove(s);
		
		// 		newOut.addAll(evalSet.get(node));
		// 		if(!newOut.equals(availOut.get(node))){
		// 			availOut.put(node, newOut);
		// 			flowChange = true;
		// 		}
		// 		/*if(debug){
		// 			System.out.println(block.label+ "new in");
		// 			for(String s: availIn.get(node))
		// 				System.out.print(s+" ");
		// 			System.out.println();
		// 			System.out.println(block.label+ "new out");
		// 			for(String s: availOut.get(node))
		// 				System.out.print(s+" ");
		// 			System.out.println();
		// 			}*/
		// 	}
		// }
		// for(NodeList node = flow.nodes(); node != null; node = node.tail){
		// 	/*			System.out.println(flow.instr(node.head).label.toString());
		// 	for(String s: availIn.get(node.head))
		// 		System.out.print(s+" ");
		// 		System.out.println();*/
		// 	subexpressionLocalElimination(flow.instr(node.head), availIn.get(node.head));
		// }
	}
	/*static void replaceCommonExpression(BasicBlock block, String s, Temp replace, BlockFlowGraph flow, Set<BasicBlock> visited )
	{
		if(visited.contains(block))return;
		visited.add(block);
		ArrayList<Instr> reverseInstr = block.toArrayListReverse();
		InstrList newInstr = null;
		boolean done = false;
		for(Instr i : reverseInstr){
			if(done == false && s.equals(i.toString())){
				Temp oldDst = ((OPER)i).dst;
				newInstr = new InstrList(new MOVE("move `d0 `s0", oldDst, replace), newInstr);
				((OPER)i).setDst(replace);
				done = true;
				change = true;
			}
			newInstr = new InstrList(i, newInstr);
		}
		if(done){
			block.setInstrs(newInstr);
		}
		else{
			Node node = flow.getNodeOfInstr(block);
			for(NodeList n = node.pred(); n != null; n = n.tail){
				replaceCommonExpression(flow.instr(n.head), s, replace, flow, visited);
			}
		}
		}*/
	/*	static void subexpressionLocalElimination(BasicBlock block, Set<String> aep, BlockFlowGraph flow, Frame.Frame frame){

		for(InstrList i = block.instrs; i != null; i = i.tail ){
			if(i.head instanceof CALL){
				return;
			}
			String assem = i.head.toString();
			if(aep.contains(assem)){
				change = true;
				OPER oper = (OPER)i.head;
				Temp replace = new Temp();
				i.head = new MOVE("move `d0 `s0", oper.dst, replace);
				Node node = flow.getNodeOfInstr(block);
				for(NodeList n = node.pred(); n != null; n = n.tail){
					replaceCommonExpression(flow.instr(n.head), assem, replace, flow, new LinkedHashSet());
				}
			}
			ArrayList<String> del = new ArrayList();
			for(TempList def = i.head.def(); def != null; def = def.tail){
				for(String s : aep){
					if(s.indexOf("("+def.head.toString()+")") >= 0)del.add(s);
				}
			}
			if(i.head.opcode == MEM.SW){
				for(String s : aep){
					if(s.startsWith("lw") && !s.endsWith("nc") && aboutStack(s, frame) == ((MEM)i.head).aboutStack())
						del.add(s);
				}
			}	
			for(String s : del)
				aep.remove(s);
		}
		block.setInstrs(block.instrs);
		}*/
	static boolean aboutStack(String s, Frame.Frame f){
		if(s.indexOf(f.FP().toString()) >= 0)return true;
		if(s.indexOf(f.FFP().toString()) >= 0)return true;
		if(s.indexOf(f.SP().toString()) >= 0)return true;
		return false;
	}

	public static void subexpressionLocalElimination(BasicBlock block, Set<InstrListTemp> avExp){
		InstrList instr = block.instrs;
		for(InstrList i = instr; i != null; i = i.tail){
			String assem = i.head.toString();
			if(!assem.equals("---")){
				OPER exp = (OPER)i.head;
				InstrListTemp it = null;
				for(InstrListTemp x : avExp)
					if(x.instr.head.toString().equals(assem)){
						it = x;
						break;
					}
				if(it != null){
					change = true;
					//					System.out.println(exp.dst.toString());
					if (it.temp != null){
						i.head = new MOVE("move `d0 `s0", exp.dst, it.temp);
					}
					else{
						OPER preexp = (OPER)it.instr.head;
						it.temp = new Temp();
						Temp predst = preexp.dst;
						preexp.setDst(it.temp);
						it.instr.tail = new InstrList(new MOVE("move `d0 `s0",predst, it.temp), it.instr.tail);
						i.head = new MOVE("move `d0 `s0", exp.dst, it.temp);
					}
				}
			}
			ArrayList<InstrListTemp> del = new ArrayList();
			if(i.head instanceof CALL){
				avExp = new LinkedHashSet();

			}
			else{
				for(TempList def = i.head.def(); def != null; def = def.tail){
					for(InstrListTemp s :avExp){
						if(s.instr.head.toString().indexOf("("+def.head.toString()+")") >= 0)
							del.add(s);
					}
				}
				if(i.head.opcode == MEM.SW){
					for(InstrListTemp s: avExp){
						if(s.instr.head.killedBySwOrCall(i.head))
							del.add(s);
					}
				}
				for(InstrListTemp d : del)
					avExp.remove(d);
			}
		}
		block.setInstrs(instr);
	}
	
	public static Set<InstrListTemp> subexpressionLocalElimination(BasicBlock block){
		InstrList instr = block.instrs;
		Map<String, InstrListTemp> avExp = new LinkedHashMap();
		for(InstrList i = instr; i != null; i = i.tail){
			String assem = i.head.toString();
			if(!assem.equals("---")){
				OPER exp = (OPER)i.head;
				InstrListTemp it = avExp.get(assem);
				if(it != null){
					change = true;
					//                                      System.out.println(exp.dst.toString());
					if (it.temp != null){
						i.head = new MOVE("move `d0 `s0", exp.dst, it.temp);
					}
					else{
						OPER preexp = (OPER)it.instr.head;
						it.temp = new Temp();
						Temp predst = preexp.dst;
						preexp.setDst(it.temp);
						it.instr.tail = new InstrList(new MOVE("move `d0 `s0",predst, it.temp), it.instr.tail);
						i.head = new MOVE("move `d0 `s0", exp.dst, it.temp);
					}
				}
				else avExp.put(assem, new InstrListTemp(i));
			}
			ArrayList<String> del = new ArrayList();
			if(i.head instanceof CALL){
				avExp = new LinkedHashMap();
			}
			else{
				for(TempList def = i.head.def(); def != null; def = def.tail){
					for(String s :avExp.keySet()){
						if(s.indexOf("("+def.head.toString()+")") >= 0)del.add(s);
					}
				}
				if(i.head.opcode == MEM.SW){
					for(Map.Entry<String, InstrListTemp> av: avExp.entrySet()){
						if(av.getValue().instr.head.killedBySwOrCall(i.head))
							del.add(av.getKey());
					}
				}
				for(String d : del)
					avExp.remove(d);
			}
		}
		block.setInstrs(instr);
		Set<InstrListTemp> eval = new LinkedHashSet();
		for(Map.Entry<String, InstrListTemp> av :avExp.entrySet())
			eval.add(av.getValue());
		return eval;
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
				LinkedHashSet<CopyPair> newIn ;
				if(pred != null){
					newIn = new LinkedHashSet(cpOut.get(pred.head));
					for(NodeList p = pred.tail; p != null; p = p.tail){
						newIn.retainAll(cpOut.get(p.head));
					}

					if(!newIn.equals(oldIn)){
						/*					System.out.println(block.label.toString() + "in");
											for(CopyPair copy : newIn){
											System.out.println(copy.to.toString() + "<="+  copy.from.toString());
											}*/
						flowChange = true;
						cpIn.put(node, newIn);
					}

				}
				else newIn = oldIn;
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