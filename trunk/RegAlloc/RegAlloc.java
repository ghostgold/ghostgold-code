package RegAlloc;
import FlowGraph.*;
import Graph.*;
import Assem.*;
import java.util.*;
public class RegAlloc implements Temp.TempMap{
	private ArrayList<BasicBlock> prog;
	private Frame.Frame frame;

	int regNum;

	public String tempMap(Temp.Temp t){
		if(t.precolored())return t.toString();
		int x = paintColor.get(interference.tnode(t)).intValue();
		//		return "$" + x;
		return frame.getReg(x).toString();
	}
		
	Set<Node> simplifyWorkList = new LinkedHashSet();
	Set<Node> freezeWorkList = new LinkedHashSet();
	Comparator<Node> spillComparator;
   	PriorityQueue<Node> spillWorkList;
	

	Set<Node> spilledNodes = new LinkedHashSet();
	Set<Node> coalescedNodes = new LinkedHashSet();
	Set<Node> coloredNodes = new LinkedHashSet();
	Stack<Node> selectStack = new Stack();
	

	Set<Node> precolored = new  LinkedHashSet();
	Set<Node> initial = new LinkedHashSet();

	Set<Move> workListMoves = new LinkedHashSet();
	Set<Move> activeMoves = new LinkedHashSet();
	Set<Move> frozenMoves = new LinkedHashSet();
	Set<Move> coalescedMoves = new LinkedHashSet();
	Set<Move> constrainedMoves = new LinkedHashSet();
		
	Map<Node, Integer> degree = new LinkedHashMap();
	Map<Node, Set<Move>> moveList = new LinkedHashMap();
	Map<Node, Node> alias = new LinkedHashMap();
	Map<Node, Integer> paintColor =new LinkedHashMap();
	
	LinkedHashSet<Integer> allColors = new LinkedHashSet();
	BlockFlowGraph controlFlow; 
	BlockLiveness liveness;
	InterferenceGraph interference;
	public RegAlloc(){
	}
	public void initialColor(){
		allColors.add(new Integer(0));
		for(int i = 2; i <= 25; i ++)
			allColors.add(new Integer(i));
		for(int i = 29; i <=31; i++)
			allColors.add(new Integer(i));
		//		for(int i = 0; i < regNum; i++)allColors.add(new Integer(i+8));
	}
	public void alloc(ArrayList<BasicBlock> instrs, int r, Frame.Frame f, boolean doSpill){
		prog = instrs;
		frame = f;
		regNum = r;
		initialColor();
		controlFlow = new BlockFlowGraph(instrs);
		liveness = new BlockLiveness(controlFlow);
		interference = new InterferenceGraph(controlFlow);

		spillComparator = new SpillComparator(interference);
		spillWorkList = new PriorityQueue<Node>(10,spillComparator);
		//		System.err.println("==============liveness done===============");
		for(Temp.TempList t = frame.registers(); t != null; t = t.tail){
			interference.newNode(t.head);
			precolored.add(interference.tnode(t.head));
			paintColor.put(interference.tnode(t.head), new Integer(t.head.precolor));
			//			System.out.println(t.head.toString()+' '+interference.tnode(t.head).toString());
		}
		NodeList nodes  = interference.nodes();
		for(NodeList t = nodes; t != null; t = t.tail)
			if(! interference.gtemp(t.head).precolored()){
				initial.add(t.head);
			}

		for(NodeList t= nodes; t != null; t = t.tail)
			moveList.put(t.head, new LinkedHashSet());
		//		System.err.println("================build begin===============");
		build();
		//		System.err.println("================build done===============");
		//		interference.show(System.out);
		makeWorkList();
		//		System.out.println("new");
		while(true){
			if(!simplifyWorkList.isEmpty()){
				simplify();
				//				System.out.println("simplify");
			}
			else if(!workListMoves.isEmpty()){
				coalesce();
				//				System.out.println("coalesce");
			}
			else if(!freezeWorkList.isEmpty()){
				freeze();
				//				System.out.println("freeze");
			}
			else if(!spillWorkList.isEmpty()){
								selectSpill(); 
				//				System.out.println("selectSpill");
			}
			if(simplifyWorkList.isEmpty() && workListMoves.isEmpty() && freezeWorkList.isEmpty()&& spillWorkList.isEmpty() )break;
		}
		assignColors();
		//		System.err.println("===============assgin done===============");
		if(doSpill && !spilledNodes.isEmpty()){
			ArrayList<BasicBlock> newInstrs = rewriteProgram();
			//			System.err.println("===============rewrite done===============");
			RegAlloc newAlloc = new RegAlloc();
			newAlloc.alloc(newInstrs, regNum, frame, doSpill);
			prog = newAlloc.getProgram();
			paintColor = newAlloc.paintColor;
			interference = newAlloc.interference;
		}

	}
	
	private Temp.TempList L(Temp.Temp h, Temp.TempList t){
		return new Temp.TempList(h,t);
	}

	ArrayList<BasicBlock> rewriteProgram(){
		Dictionary<Node, Frame.Access> tempSpillMem =new Hashtable();
		Iterator<Node> it = spilledNodes.iterator();
		while(it.hasNext())
			tempSpillMem.put(it.next(), frame.allocLocal(true));
		ArrayList<BasicBlock> blocks = prog;
		for(ListIterator<BasicBlock> b = blocks.listIterator(); b.hasNext(); ){
			BasicBlock block = b.next();
			InstrList ins = block.instrs;
			while(ins != null){
				if(ins.head instanceof MOVE){
					MOVE move = (MOVE)(ins.head);
					Temp.Temp dst = move.dst;
					if( dst != null && spilledNodes.contains(interference.tnode(dst))){
						Temp.Temp src = move.src;
						Frame.Access mem = tempSpillMem.get(interference.tnode(dst));
						ins.head = new MEM("sw `s0 `i(`s1)", null , src, frame.FP(), mem.offSet(), frame, Assem.MEM.SW);
					}
				}
				else if(ins.head instanceof OPER){
					OPER op = (OPER)(ins.head);
					Temp.Temp def = op.dst;
					if( def != null && spilledNodes.contains(interference.tnode(def))){
						Frame.Access mem = tempSpillMem.get(interference.tnode(def));
						Temp.Temp spillTemp = new Temp.Temp();
						spillTemp.setSpillCost(-1);
						op.setDst(spillTemp);
						ins.tail = new InstrList(new MEM("sw `s0 `i(`s1)",null, spillTemp, frame.FP(), 
														 mem.offSet(), frame, Assem.MEM.SW),
												 ins.tail);
						ins = ins.tail;
					}
				}
				if(ins.tail != null){
					if(ins.tail.head instanceof MOVE){
						MOVE move = (MOVE)(ins.tail.head);
						Temp.Temp src = move.src;
						if(src != null && spilledNodes.contains(interference.tnode(src))){
							Temp.Temp dst = move.dst;
							Frame.Access mem = tempSpillMem.get(interference.tnode(src));
							ins.tail.head = new MEM("lw `d0 `i(`s0)", dst, frame.FP(), null, mem.offSet(), frame,
													Assem.MEM.LW);
						}
					}
					else if(ins.tail.head instanceof OPER){
						OPER op = (OPER)(ins.tail.head);
						Temp.Temp left = op.left;
						Temp.Temp spillLeft = left;
						if(left != null && spilledNodes.contains(interference.tnode(left))){
							Frame.Access mem = tempSpillMem.get(interference.tnode(left));
							spillLeft = new Temp.Temp();
							spillLeft.setSpillCost(-1);
							ins.tail = new InstrList(new MEM("lw `d0 `i(`s0)", spillLeft, frame.FP(), null, 
															 mem.offSet(), frame, Assem.MEM.LW), ins.tail);
							ins = ins.tail;
						}
						Temp.Temp right = op.right;
						Temp.Temp spillRight = right;
						if(right != null && spilledNodes.contains(interference.tnode(right))){
							if(left != right){
								Frame.Access mem = tempSpillMem.get(interference.tnode(right));
								spillRight = new Temp.Temp();
								spillLeft.setSpillCost(-1);
								ins.tail = new InstrList(new MEM("lw `d0 `i(`s0)", spillRight, frame.FP(), null, 
																 mem.offSet(), frame, Assem.MEM.LW), ins.tail);
								ins = ins.tail;
							}
							else spillRight = spillLeft;
						}
						op.setSrc(spillLeft, spillRight);
					}
				}
				ins = ins.tail;
			}
			block.setInstrs(block.instrs);
		}
		prog.get(0).instrs.tail.tail.tail.head.assem = "addi `d0 `s0 -"+frame.framesize();
		return prog;
	}
	void build(){
		ArrayList<BasicBlock> blocks = prog;
		for(NodeList nodes = interference.nodes(); nodes != null; nodes = nodes.tail){
			degree.put(nodes.head, new Integer(0));
		}
		//		Dictionary<Assem.Instr,Set<Temp.Temp>> liveOut;
		for(BasicBlock block : blocks){
			for(Temp.TempList tot = block.tot(); tot != null; tot = tot.tail)
						tot.head.addSpillCost(1);
			Set<Temp.Temp> live = liveness.liveAt(block);
			/*			while(l.hasNext()){
				System.out.print(l.next().toString() + ' ');
			}
			System.out.println();*/

		/*			System.out.println("liveout");
			for(Temp.TempList l= live; l != null; l = l.tail){
				System.out.print(l.head.toString()+" ");
				
				}
				System.out.println();*/
			InstrList t = block.reverseInstrs;
			while(t != null){
				if(t.head instanceof MOVE){
					MOVE move = (MOVE)t.head;
					Node dstNode = interference.tnode(move.dst);
					Node srcNode = interference.tnode(move.src);
					live.remove(interference.gtemp(srcNode)); 
					//					live.remove(move.src);
					Move nodeMove = new Move(dstNode, srcNode);
					moveList.get(dstNode).add(nodeMove);
					moveList.get(srcNode).add(nodeMove);
					workListMoves.add(nodeMove);
				}
				for(Temp.TempList def = t.head.def(); def != null ; def = def.tail)
					live.add(def.head);
				for(Temp.TempList def = t.head.def(); def != null; def = def.tail){
					for(Temp.Temp u : live){
						if(def.head != u)
							addEdge(interference.tnode(def.head), interference.tnode(u));
					}
				}
			//			interference.show(System.out);
			//			System.out.println("=============\n");
				for(Temp.TempList def = t.head.def(); def != null ; def = def.tail)
					live.remove(def.head);
				for(Temp.TempList use = t.head.use(); use != null ; use = use.tail)
					live.add(use.head);
				t = t.tail;
			}

		}
		for(Node node: precolored){
			degree.put(node, new Integer(10000000));
		}
		Iterator<Node> a = precolored.iterator();

		while(a.hasNext()){
			Node x = a.next();
			Iterator<Node> b= precolored.iterator();
			while(b.hasNext()){
				Node y = b.next();
				if(x != y)addEdge(x,y);
			}
		}
	}

	void addEdge(Node a, Node b){
		if(!interference.queryEdge(a,b) && a != b){
			interference.addEdge(a,b);
			int d = degree.get(a).intValue();
			degree.put(a, new Integer(d+1));
			d = degree.get(b).intValue();
			degree.put(b, new Integer(d+1));
		}
	}
	void makeWorkList(){
		for(Node node: initial){
			if(degree.get(node).intValue() >= regNum)
				spillWorkList.add(node);
			else if( moveRelated(node))
				freezeWorkList.add(node);
			else
				simplifyWorkList.add(node);
		}
	}

	void simplify(){
		Iterator<Node> it = simplifyWorkList.iterator();
		Node node = null;
		if(it.hasNext())node = it.next();
		if(node != null){
			simplifyWorkList.remove(node);
			selectStack.push(node);
		}
		for(NodeList t = adj(node); t != null; t = t.tail){
			//might be opt, put check for select and coalesced here
			decrementDegree(t.head);
		}
	}

	void decrementDegree(Node m) {
		int d = degree.get(m).intValue();
		degree.put(m, new Integer(d-1));
		if (d == regNum){
			enableMoves(new NodeList(m, adj(m)));
			spillWorkList.remove(m);
			if (moveRelated(m))
				freezeWorkList.add(m);
			else 
				simplifyWorkList.add(m);
		}
	}
	void enableMoves(NodeList nodes){
		for(NodeList a = nodes; a != null; a = a.tail){
			MoveList moves = nodeMoves(a.head);
			while(moves != null){
				if(activeMoves.contains(moves.head)){
					activeMoves.remove(moves.head);
					workListMoves.add(moves.head);
				}
				moves = moves.tail;
			}
		}
	}

	void coalesce(){
		//doing
		Iterator<Move> it = workListMoves.iterator();
		Move move = null;
		if(it.hasNext())move = it.next();
		if(move == null)throw new Error("no move in workListMoves");
		Node x = getAlias(move.dst);
		Node y = getAlias(move.src);
		Node u;
		Node v;
		if(precolored.contains(y)){
			u = y;
			v = x;
		}
		else {
			u = x;
			v = y;
		}
		workListMoves.remove(move);
		if(u == v){
			coalescedMoves.add(move);
			addWorkList(u);
		}
		else if(precolored.contains(v) || interference.queryEdge(u,v)){
			constrainedMoves.add(move);
			addWorkList(u);
			addWorkList(v);
		}
		else {
			boolean condition1 = false;
			boolean condition2 = false;
			if(precolored.contains(u)){
				condition1 = true;
				for(NodeList nodes = adj(v); nodes != null; nodes = nodes.tail)
					if(!OK(nodes.head, u))condition1 = false;
			}
			else{
				condition2 = true;
				Set<Node> tset = new  LinkedHashSet();
				for(NodeList nodes = adj(u); nodes != null; nodes = nodes.tail)
					tset.add(nodes.head);
				for(NodeList nodes = adj(v); nodes != null; nodes = nodes.tail)
					tset.add(nodes.head);
				if(!conservative(tset))
					condition2 = false;
			}
			if(condition1 || condition2){
				coalescedMoves.add(move);
				combine(u,v);
				addWorkList(u);
			}
			else activeMoves.add(move);
		}
	}

	void addWorkList(Node u){

		if(!precolored.contains(u) && !moveRelated(u) && degree.get(u).intValue() < regNum){
			freezeWorkList.remove(u);
			simplifyWorkList.add(u);
		}
	}

	void selectSpill(){
		Node m = spillWorkList.peek();
		if(m != null){
			spillWorkList.remove(m);
			simplifyWorkList.add(m);
			freezeMoves(m);
		}
		
		
	}
	boolean OK(Node t, Node r){
		return (degree.get(t).intValue()< regNum || precolored.contains(t) || interference.queryEdge(t,r));
	}
	
	boolean conservative(Set<Node> nodes){
		Iterator<Node> it = nodes.iterator();
		int total = 0;
		while(it.hasNext()){
			if(degree.get(it.next()).intValue() >= regNum)total++;
		}
		return (total < regNum);
	}

	Node getAlias(Node n){
		//binchaji? path compress
		if(coalescedNodes.contains(n))
			return getAlias(alias.get(n));
		return n;
	}

	void combine(Node u, Node v){
		if(freezeWorkList.contains(v))
			freezeWorkList.remove(v);
		else 
			spillWorkList.remove(v);
		coalescedNodes.add(v);
		alias.put(v,u);
		moveList.get(u).addAll(moveList.get(v));
		enableMoves(new NodeList(v,null));// from the errata. Don't understand
		for(NodeList nodes = adj(v); nodes != null; nodes = nodes.tail){
			addEdge(nodes.head, u);
			decrementDegree(nodes.head);
		}
		
	}

	void freeze(){
		Iterator<Node> it = freezeWorkList.iterator();
		if(it.hasNext()){
			Node node = it.next();
			freezeWorkList.remove(node);
			simplifyWorkList.add(node);
			freezeMoves(node);
		}
	}

	void freezeMoves(Node u){
		for(MoveList moves = nodeMoves(u); moves != null; moves = moves.tail){
			Move move = moves.head;
			Node v = null;
			if(getAlias(move.dst) == getAlias(u))
				v = getAlias(move.src);
			else v = getAlias(move.dst);
			activeMoves.remove(move);
			frozenMoves.add(move);
			if(nodeMoves(v) == null && degree.get(v).intValue() < regNum){
				freezeWorkList.remove(v);
				simplifyWorkList.add(v);
			}
		}
	}

	
	void assignColors(){
		while(!selectStack.empty()){
			Node node = selectStack.pop();
			//			System.out.println(interference.gtemp(node));
			LinkedHashSet<Integer> colors = (LinkedHashSet)allColors.clone();
			for(NodeList l = node.succ(); l != null; l = l.tail){
				Node w = getAlias(l.head);
				if(coloredNodes.contains(w) || precolored.contains(w) ){
					colors.remove(paintColor.get(w));
				}
			}
			if(colors.isEmpty()){
				//				System.out.println(interference.gtemp(node));
				spilledNodes.add(node);
			}
			else {
				coloredNodes.add(node);
				paintColor.put(node, colors.iterator().next());
			}
		}
		Iterator<Node> it = coalescedNodes.iterator();
		while(it.hasNext()){
			Node node = it.next();
			paintColor.put(node, paintColor.get(getAlias(node)));
		}
	}
 	NodeList adj(Node n){
		NodeList ans = null;
		for(NodeList t = n.succ(); t != null; t = t.tail){
			if(selected(t.head) || coalesced(t.head))continue;
			else ans = new NodeList(t.head, ans);
		}
		return ans;
	}

	boolean selected(Node n){
		return selectStack.contains(n);
	}
	boolean coalesced(Node n){
		return coalescedNodes.contains(n);
	}
	MoveList nodeMoves(Node n){
		Set<Move> s = moveList.get(n);
		Iterator<Move> it = s.iterator();
		MoveList ans = null;
		while(it.hasNext()){
			Move move = it.next();
			if(activeMoves.contains(move) || workListMoves.contains(move)){
				ans = new MoveList(move, ans);
			}
		}
		return ans;
	}
	boolean moveRelated(Node node){
		if(nodeMoves(node) == null)return false;
		else return true;
	}
	
	
	public ArrayList<BasicBlock> getProgram(){
		return prog;
	}

}