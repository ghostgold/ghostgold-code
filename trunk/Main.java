import Parse.*;
import Semant.*;
import java.util.*;
public class Main {

	public static void main(String argv[])
		throws java.io.IOException, java.lang.Exception{
		String filename = argv[0];
		ErrorMsg.ErrorMsg errorSender= new ErrorMsg.ErrorMsg(filename);
		java.io.InputStream inp=new java.io.FileInputStream(filename);
		
		//===========================================================
		boolean printAbsyn = false;
		boolean printIR = false;
		boolean printAssem = true;
		boolean allocTemp = true;
		//=========================================================

		Lexer scanner = new Lexer(inp,errorSender);
		Parser par =new Parser(scanner);
		java_cup.runtime.Symbol parseTree;
		Absyn.Exp absyn;
		Absyn.Print printer; 
		Frame.Frame topFrame;
		Translate.Level top;
		Semant semant;
		ExpTy irTreeWithType;

		//try{
			parseTree = (par.parse());
			absyn = (Absyn.Exp)(parseTree.value);
			printer = new Absyn.Print(System.out);
			if(printAbsyn){
				printer.prExp(absyn,4);
				System.out.print("\n==============================\n\n");
			}
			FindEscape.FindEscape findEscape = new FindEscape.FindEscape();
			int mainOut = findEscape.traverseExp(0,absyn);
			topFrame = new Mips.MipsFrame(new Temp.Label("main", false), null, mainOut);
			top = new Translate.Level(topFrame);
			semant = new Semant(errorSender, top);
			semant.init();
			irTreeWithType = semant.transExp(absyn, null);
			//		}
		/*		catch(Exception e)
			{
				System.out.println("error first");
				return ;
				}*/
		if(Semant.semantError){
			System.exit(1);
		}

		Tree.Print irPrinter = new Tree.Print(System.out);
		Temp.TempMap defaultMap = new Temp.DefaultMap();
		
		for(Translate.Frag f = semant.translate.getResults(); f != null; f = f.next ){
			if(f instanceof Translate.DataFrag){
				System.out.println(".data");
				String s = ((Translate.DataFrag)f).data;
				System.out.println( ((Translate.DataFrag)f).label.toString()+":");
				System.out.print(".byte ");
				for(int i = 0 ; i < s.length(); i++){
					System.out.print((int)s.charAt(i));
					System.out.print(",");
				}
				System.out.println("0");
				System.out.println(".align 2");
				
			}
			else {

				Tree.Stm stm = ((Translate.ProcFrag)f).body;
				Frame.Frame funcframe= ((Translate.ProcFrag)f).frame;
				funcframe.procEntryExit1(stm);
				Canon.TraceSchedule trace = new Canon.TraceSchedule(new Canon.BasicBlocks(Canon.Canon.linearize(stm)));
				
				Tree.StmList  stmlist = trace.stms;
				//irPrinter.prStm(stmlist.head);
				//Assem.BasicBlock 
				Assem.InstrList assem = funcframe.codegen(stmlist.head);			
				stmlist = stmlist.tail;
				while(stmlist != null){		
					if(printIR){
						irPrinter.prStm(stmlist.head);
						System.out.println("------------------------\n");
					}
					assem.append(funcframe.codegen(stmlist.head));
					stmlist = stmlist.tail;
				} 
				RegAlloc.RegAlloc regmap = new RegAlloc.RegAlloc();
				assem = funcframe.procEntryExit2(assem);
				assem = funcframe.procEntryExit3(assem);
				ArrayList<Assem.BasicBlock> basicblocks = Assem.BasicBlock.Partition(assem);
				if(allocTemp){
					regmap.alloc(basicblocks,28,funcframe,true);
					basicblocks = regmap.getProgram();
				}
				Temp.TempMap tempmap;
				if(allocTemp)tempmap = regmap;
				else tempmap = defaultMap;

				if(printAssem){
					System.out.println(".text");
					for(ListIterator<Assem.BasicBlock> t = basicblocks.listIterator(); t.hasNext();){
						assem = t.next().instrs;
						while(assem != null){
							System.out.println(assem.head.format(tempmap));
							assem = assem.tail;
						}
						System.out.println("\n");
					}
				}
			}
			System.out.println();
		}
		//		if(printIR)irPrinter.prStm(irTreeWithType.exp.unNx());
		Tree.Stm stm = irTreeWithType.exp.unNx();
		Frame.Frame funcframe= topFrame;
		if(printIR)irPrinter.prStm(stm);
		funcframe.procEntryExit1(stm);
		//				Tree.StmList stmlist = Canon.Canon(stm);
		Canon.TraceSchedule trace = new Canon.TraceSchedule(new Canon.BasicBlocks(Canon.Canon.linearize(stm)));
		Tree.StmList  stmlist = trace.stms;
		Assem.InstrList assem = funcframe.codegen(stmlist.head);			
		if(printIR)irPrinter.prStm(stmlist.head);
		stmlist = stmlist.tail;
		while(stmlist != null){
			if(printIR)irPrinter.prStm(stmlist.head);
			//			irPrinter.prStm(stmlist.head);
			assem.append(funcframe.codegen(stmlist.head));
			stmlist = stmlist.tail;
		}
		RegAlloc.RegAlloc regmap = new RegAlloc.RegAlloc();
		assem.append(new Assem.InstrList(new Assem.CALL("jal _exit", null, new Temp.TempList(funcframe.ZERO(), new Temp.TempList(funcframe.SP(), null)), null), null));
		assem = funcframe.procEntryExit3(assem);
		
		ArrayList<Assem.BasicBlock> basicblocks = Assem.BasicBlock.Partition(assem);

		if(allocTemp){regmap.alloc(basicblocks,28,topFrame,true);
			basicblocks = regmap.getProgram();
		}
		Temp.TempMap tempmap;
		if(allocTemp)tempmap = regmap;
		else tempmap = defaultMap;
		if(printAssem){
			System.out.println(".text");
			for(ListIterator<Assem.BasicBlock> t = basicblocks.listIterator(); t.hasNext();){
				assem = t.next().instrs;
				while(assem != null){
					System.out.println(assem.head.format(tempmap));
					assem = assem.tail;
				}
				System.out.println("\n");
			}
		}
		return;
	}

}


