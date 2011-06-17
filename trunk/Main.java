import Parse.*;
import Semant.*;
import java.util.*;
import java.io.*;

public class Main {

	public static void main(String argv[])
		throws java.io.IOException, java.lang.Exception{
		String filename = argv[0];
		String suffix = ".tig";
		ErrorMsg.ErrorMsg errorSender = new ErrorMsg.ErrorMsg(filename);
		java.io.InputStream inp = new java.io.FileInputStream(filename);
		if (!filename.endsWith(suffix)){
			System.out.println("filename should be end with .tig");
			return;
		}
		//===========================================================
		boolean printAbsyn = false;
		boolean printIR = false;
		boolean printAssem = true;
		boolean allocTemp = true;
		boolean absynOpt = true;
		boolean irOpt = true;
		boolean flowOpt = false;
		//=========================================================

		Lexer scanner = new Lexer(inp,errorSender);
		Parser par = new Parser(scanner);
		java_cup.runtime.Symbol parseTree;
		Absyn.Exp absyn;
		Absyn.Print printer; 
		Frame.Frame topFrame;
		Translate.Level top;
		Semant semant;
		ExpTy irTreeWithType;
		
		//==============================front end begin===================
		
		//		try {
			parseTree = (par.parse());
			absyn = (Absyn.Exp)(parseTree.value);
			printer = new Absyn.Print(System.out);
			AbsynOpt.ReNaming rename  = new AbsynOpt.ReNaming(null);
			if(absynOpt){
				absyn = rename.renameExp(absyn);				System.out.println("rename done");
				absyn = AbsynOpt.Inline.inlineExp(absyn);				System.out.println("inline done");
				AbsynOpt.FindConst.findConstExp(absyn);				System.out.println("find const done");
				absyn = AbsynOpt.CopyConst.copyConstExp(absyn);				System.out.println("copy const done");
				absyn = AbsynOpt.LoopExpantion.loopExpantion(absyn);				System.out.println("loop expantion done");

			}
			if(printAbsyn){
				printer.prExp(absyn,4);
				System.out.print("\n==============================\n\n");
				return;
			}
			
			FindEscape.FindEscape findEscape = new FindEscape.FindEscape();
			int mainOut = findEscape.traverseExp(0,absyn);
			topFrame = new Mips.MipsFrame(new Temp.Label("main", false), null, mainOut);
			top = new Translate.Level(topFrame);
			semant = new Semant(errorSender, top);
			semant.init();
			irTreeWithType = semant.transExp(absyn, null);
			//		}
			/*catch(Exception e){
			System.out.println("error first");
			return;
			}*/
		if(Semant.semantError){
			System.exit(1);
		}

		//=====================================front end finish================
		//=====================================back end begin==================
		Tree.Print irPrinter = new Tree.Print(System.out);
		Temp.TempMap defaultMap = new Temp.DefaultMap();
		String assemname = filename.substring(0, filename.length() - suffix.length()) + ".s";
		PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(assemname)));
		for(Translate.Frag f = semant.translate.getResults(); f != null; f = f.next ){
			if(f instanceof Translate.DataFrag){
				out.println(".data");
				String s = ((Translate.DataFrag)f).data;
				out.println( ((Translate.DataFrag)f).label.toString()+":");
				out.print(".byte ");
				for(int i = 0 ; i < s.length(); i++){
					out.print((int)s.charAt(i));
					out.print(",");
				}
				out.println("0");
				out.println(".align 2");
			}
			else {
				Tree.Stm stm = ((Translate.ProcFrag)f).body;
				Frame.Frame funcframe= ((Translate.ProcFrag)f).frame;
				funcframe.procEntryExit1(stm);
				Canon.TraceSchedule trace = new Canon.TraceSchedule(new Canon.BasicBlocks(Canon.Canon.linearize(stm)));
				Tree.StmList  stmlist = trace.stms;
				if(irOpt)
					for(Tree.StmList s = stmlist; s!= null; s = s.tail)
						s.head = Tree.ConstFolding.constFolding(s.head);
				Assem.InstrList assem = funcframe.codegen(stmlist.head);			
				stmlist = stmlist.tail;
				//				System.out.println(funcframe.getName().toString());
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
				if(flowOpt)RegAlloc.FlowOpt.flowOpt(basicblocks, funcframe);

				if(allocTemp){
					regmap.alloc(basicblocks,28,funcframe,true);
					basicblocks = regmap.getProgram();
				}

				Temp.TempMap tempmap;
				if(allocTemp)tempmap = regmap;
				else tempmap = defaultMap;

				if(printAssem){
					out.println(".text");
					for(ListIterator<Assem.BasicBlock> t = basicblocks.listIterator(); t.hasNext();){
						assem = t.next().instrs;
						while(assem != null){
							out.println(assem.head.format(tempmap));
							assem = assem.tail;
						}
						out.println("\n");
					}
				}
			}
			out.println();
		}

		Tree.Stm stm = irTreeWithType.exp.unNx();
		Frame.Frame funcframe= topFrame;
		if(printIR)irPrinter.prStm(stm);
		funcframe.procEntryExit1(stm);
		Canon.TraceSchedule trace = new Canon.TraceSchedule(new Canon.BasicBlocks(Canon.Canon.linearize(stm)));
		Tree.StmList  stmlist = trace.stms;
		if(irOpt)
			for(Tree.StmList s = stmlist; s!= null; s = s.tail)
				s.head = Tree.ConstFolding.constFolding(s.head);
		Assem.InstrList assem = funcframe.codegen(stmlist.head);			
		//		System.out.println(funcframe.getName().toString());
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
		if(flowOpt)RegAlloc.FlowOpt.flowOpt(basicblocks, funcframe);
		if(allocTemp){regmap.alloc(basicblocks,28,topFrame,true);
			basicblocks = regmap.getProgram();
		}
		Temp.TempMap tempmap;
		if(allocTemp)tempmap = regmap;
		else tempmap = defaultMap;
		if(printAssem){
			out.println(".text");
			for(ListIterator<Assem.BasicBlock> t = basicblocks.listIterator(); t.hasNext();){
				assem = t.next().instrs;
				while(assem != null){
					out.println(assem.head.format(tempmap));
					assem = assem.tail;
				}
				out.println("\n");
			}
		}
		new RuntimePrinter(out);
		out.close();
		return;
	}


}


class RuntimePrinter{
	RuntimePrinter(PrintStream out){
		try{
			String runtimeName = "runtime.s";
			InputStream runtime = getClass().getResourceAsStream(runtimeName);
			BufferedReader runtimeReader = new BufferedReader(new InputStreamReader(runtime));
			String line = runtimeReader.readLine();
			while(line != null){
				out.println(line);
				line = runtimeReader.readLine();
			}
		}
		catch(IOException e){
			System.out.println("No Runtime");
		}
		
	}
}