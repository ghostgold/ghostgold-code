package RegAlloc;
import FlowGraph.*;
import Graph.*;
import Assem.*;
import java.util.*;
public class RegAlloc implements Temp.TempMap{
	private InstrList prog;
	private Frame.Frame frame;

	int regNum;

	public String tempMap(Temp.Temp t){
		if(t.precolored())return t.toString();
		int x = paintColor.get(interference.tnode(t)).intValue();
		//		return "$" + x;
		return frame.getReg(x).toString();
	}
		
	Set<Node> simplifyWorkList = new HashSet();
	Set<Node> freezeWorkList = new HashSet();
	Comparator<Node> spillComparator;
   	PriorityQueue<Node> spillWorkList;
	

	Set<Node> spilledNodes = new HashSet();
	Set<Node> coalescedNodes = new HashSet();
	Set<Node> coloredNodes = new HashSet();
	Stack<Node> selectStack = new Stack();
	

	Set<Node> precolored = new  HashSet();
	Set<Node> initial = new HashSet();

	Set<Move> workListMoves = new HashSet();
	Set<Move> activeMoves = new HashSet();
	Set<Move> frozenMoves = new HashSet();
	Set<Move> coalescedMoves = new HashSet();
	Set<Move> constrainedMoves = new HashSet();
		
	Dictionary<Node, Integer> degree = new Hashtable();
	Dictionary<Node, Set<Move>> moveList = new Hashtable();
	Dictionary<Node, Node> alias = new Hashtable();
	Dictionary<Node, Integer> paintColor =new Hashtable();
	
	HashSet<Integer> allColors = new HashSet();
	AssemFlowGraph controlFlow; 
	AssemLiveness liveness;
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
	public void alloc(InstrList instrs, int r, Frame.Frame f, boolean doSpill){
		prog = instrs;
		frame = f;
		regNum = r;
		initialColor();
		controlFlow = new AssemFlowGraph(instrs);
		liveness = new AssemLiveness(controlFlow);
		interference = new InterferenceGraph(controlFlow);

		spillComparator = new SpillComparator(interference);
		spillWorkList = new PriorityQueue<Node>(10,spillComparator);

		for(Temp.TempList t = frame.registers(); t != null; t = t.tail){
			interference.newNode(t.head);
			precolored.add(interference.tnode(t.head));
			paintColor.put(interference.tnode(t.head), new Integer(t.head.precolor));
			//			System.out.println(t.head.toString()+' '+interference.tnode(t.head).toString());
		}
		NodeList nodes  = interference.nodes();
		for(NodeList t = nodes; t != null; t = t.tail)
			if(! interference.gtemp(t.head).precolored())initial.add(t.head);
		for(NodeList t= nodes; t != null; t = t.tail)
			moveList.put(t.head, new HashSet());
		build();
		//		interference.show(System.out);
		makeWorkList();
		while(true){
			if(!simplifyWorkList.isEmpty())simplify();
			else if(!workListMoves.isEmpty())coalesce();
			else if(!freezeWorkList.isEmpty())freeze();
			else if(!spillWorkList.isEmpty())selectSpill(); 
			if(simplifyWorkList.isEmpty() && workListMoves.isEmpty() && freezeWorkList.isEmpty()&& spillWorkList.isEmpty() )break;
		}
		assignColors();
		if(doSpill && !spilledNodes.isEmpty()){
			InstrList newInstrs = rewriteProgram();
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

	InstrList rewriteProgram(){
		Dictionary<Node, Frame.Access> tempSpillMem =new Hashtable();
		Iterator<Node> it = spilledNodes.iterator();
		while(it.hasNext())
			tempSpillMem.put(it.next(), frame.allocLocal(true));
		InstrList ins = prog;
		while(ins != null){
			Temp.TempList def = ins.head.def();
			if( def != null && spilledNodes.contains(interference.tnode(def.head))){
				Frame.Access mem = tempSpillMem.get(interference.tnode(def.head));
				if(ins.head instanceof MOVE){
					ins.head = new OPER("sw `s0 " + mem.offSet() +"(`s1)",null,  L(ins.head.use().head, L(frame.FP(), null)));
				}
				else {
					Temp.Temp spillTemp = new Temp.Temp();
					spillTemp.setSpillCost(-1);
					ins.head.setDef(L(spillTemp,null));
					ins.tail = new InstrList(new OPER("sw `s0 " + mem.offSet() +"(`s1)",null, L(spillTemp, L(frame.FP(),null))), ins.tail);
					ins = ins.tail;
				}
			}
			if(ins.tail != null){
				if(ins.tail.head instanceof MOVE){
					Temp.TempList use = ins.tail.head.use();
					if(spilledNodes.contains(interference.tnode(use.head))){
						Frame.Access mem = tempSpillMem.get(interference.tnode(use.head));
						ins.tail.head = new OPER("lw `d0 "+mem.offSet() + "(`s0)", ins.tail.head.def(), L(frame.FP(), null) );
					}
				}
				else{
					Temp.TempList newUse = new Temp.TempList(null, null);
					Temp.TempList saveUse = newUse;
					for(Temp.TempList use = ins.tail.head.use(); use != null; use = use.tail){
						if(spilledNodes.contains(interference.tnode(use.head))){
							Frame.Access mem = tempSpillMem.get(interference.tnode(use.head));
							Temp.Temp spillTemp = new Temp.Temp();
							spillTemp.setSpillCost(-1);
							if(newUse.head == null)newUse.head = spillTemp;
							else {
								newUse.tail = L(spillTemp,null);
								newUse = newUse.tail;
							}
							ins.tail = new InstrList(new OPER("lw `d0 "+mem.offSet() + "(`s0)", L(spillTemp,null), L(frame.FP(), null) ), ins.tail);
							ins = ins.tail;
						}
						else {
							if(newUse.head == null)newUse.head = use.head;
							else {
								newUse.tail = L(use.head,null);
								newUse = newUse.tail;
							}
						}

					}
					if(saveUse.head != null)ins.tail.head.setUse(saveUse);
				}
			}
			ins = ins.tail;
		}
		prog.tail.tail.tail.head.assem = "addi `d0 `s0 -"+frame.framesize();
		return prog;
	}
	void build(){
		InstrList instrs = prog;
		for(NodeList nodes = interference.nodes(); nodes != null; nodes = nodes.tail){
			degree.put(nodes.head, new Integer(0));
		}
		for(InstrList t = instrs; t != null; t = t.tail){
			Temp.TempList live = liveness.liveAt(t.head);
			for(Temp.TempList def = t.head.def(); def != null; def = def.tail)
				def.head.addSpillCost(1);
			for(Temp.TempList use = t.head.use(); use != null; use = use.tail)
				use.head.addSpillCost(1);
			
			/*			System.out.println("liveout");
			for(Temp.TempList l= live; l != null; l = l.tail){
				System.out.print(l.head.toString()+" ");
				
				}
				System.out.println();*/
			if(t.head instanceof MOVE){
				MOVE move = (MOVE)t.head;
				Node dstNode = interference.tnode(move.dst);
				Node srcNode = interference.tnode(move.src);
				for(Temp.TempList l= live; l != null; l = l.tail){
					if(l.head != move.dst && l.head != move.src){
						addEdge(interference.tnode(l.head), dstNode);
					}
				}
				Move nodeMove = new Move(dstNode, srcNode);
				moveList.get(dstNode).add(nodeMove);
				moveList.get(srcNode).add(nodeMove);
				workListMoves.add(nodeMove);
			}
			else{
				for(Temp.TempList def = t.head.def(); def != null; def = def.tail)
					for(Temp.TempList l = live; l != null; l = l.tail)
						if(def.head != l.head)
							addEdge(interference.tnode(def.head), interference.tnode(l.head));
			}
			//			interference.show(System.out);
			//			System.out.println("=============\n");

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
		Iterator<Node> t = initial.iterator();
		while(t.hasNext()){
			Node node = t.next();
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

	void decrementDegree(Node m){
		int d = degree.get(m).intValue();
		degree.put(m, new Integer(d-1));
		if(d == regNum){
			enableMoves(new NodeList(m, adj(m)));
			spillWorkList.remove(m);
			if(moveRelated(m))
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
				Set<Node> tset = new  HashSet();
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

	void selectSApill(){
		//todo
	}
	
	void assignColors(){
		while(!selectStack.empty()){
			Node node = selectStack.pop();
			HashSet<Integer> colors = (HashSet)allColors.clone();
			for(NodeList l = node.succ(); l != null; l = l.tail){
				Node w = getAlias(l.head);
				if(coloredNodes.contains(w) || precolored.contains(w) ){
					colors.remove(paintColor.get(w));
				}
			}
			if(colors.isEmpty())
				spilledNodes.add(node);
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
	
	
	public InstrList getProgram(){
		return prog;
	}

}