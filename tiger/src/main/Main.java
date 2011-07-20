package main;
import java.util.*;
import java.io.*;

import parse.*;
import semant.*;

public class Main {

	public static void main(String argv[])
		throws java.io.IOException, java.lang.Exception{
		//===========================================================
		boolean printAbsyn = false;
		boolean printIR = false;
		boolean printAssem = true;
		boolean allocTemp = true;
		boolean absynOptSwitch = true;
		boolean irOptSwitch = false;
		boolean flowOptSwitch = false ;
		//=========================================================

		String suffix = ".tig";
		String filename = "";
		for(String s: argv){
			if(s.endsWith(suffix))
				 filename = s;
			if(s.equals("-O")){
				absynOptSwitch = true;
				irOptSwitch = true;
				flowOptSwitch = false;
			}
		}


		if (!filename.endsWith(suffix)){
			System.out.println("filename should be end with .tig");
			return;
		}

		errormsg.ErrorMsg errorSender = new errormsg.ErrorMsg(filename);
		java.io.InputStream inp = new java.io.FileInputStream(filename);

		Lexer scanner = new Lexer(inp,errorSender);
		Parser par = new Parser(scanner);
		java_cup.runtime.Symbol parseTree;
		absyn.Exp absyn;
		absyn.Print printer; 
		frame.Frame topFrame;
		translate.Level top;
		Semant semant;
		ExpTy irTreeWithType;
		
		//==============================front end begin===================
		
		//		try {
			parseTree = (par.parse());
			absyn = (absyn.Exp)(parseTree.value);
			printer = new absyn.Print(System.out);
			absynopt.ReNaming rename  = new absynopt.ReNaming(null);
			if(absynOptSwitch){
				absyn = rename.renameExp(absyn);
				//System.out.println("rename done");
				absyn = absynopt.Inline.inlineExp(absyn);				
				//System.out.println("inline done");
				absynopt.FindConst.findConstExp(absyn);
				//System.out.println("find const done");
				absyn = absynopt.CopyConst.copyConstExp(absyn);				
				//System.out.println("copy const done");
				absyn = absynopt.LoopExpantion.loopExpantion(absyn);
				//System.out.println("loop expantion done");
			}
			if(printAbsyn){
				printer.prExp(absyn,4);
				System.out.print("\n==============================\n\n");
				return;
			}
			
			findescape.FindEscape findEscape = new findescape.FindEscape();
			int mainOut = findEscape.traverseExp(0,absyn);
			topFrame = new mips.MipsFrame(new temp.AtomicLabel("main", false), null, mainOut);
			top = new translate.Level(topFrame);
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
		tree.Print irPrinter = new tree.Print(System.out);
		temp.TempMap defaultMap = new temp.DefaultMap();
		String assemname = filename.substring(0, filename.length() - suffix.length()) + ".s";
		PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(assemname)));
		for(translate.Frag f = semant.translate.getResults(); f != null; f = f.next ){
			if(f instanceof translate.DataFrag){
				out.println(".data");
				String s = ((translate.DataFrag)f).data;
				out.println( ((translate.DataFrag)f).label.toString()+":");
				out.print(".byte ");
				for(int i = 0 ; i < s.length(); i++){
					out.print((int)s.charAt(i));
					out.print(",");
				}
				out.println("0");
				out.println(".align 2");
			}
			else {
				tree.Stm stm = ((translate.ProcFrag)f).body;
				frame.Frame funcframe= ((translate.ProcFrag)f).frame;
				funcframe.procEntryExit1(stm);
				canon.TraceSchedule trace = new canon.TraceSchedule(new canon.BasicBlocks(canon.Canon.linearize(stm)));
				tree.StmList  stmlist = trace.stms;
				if(irOptSwitch)
					for(tree.StmList s = stmlist; s!= null; s = s.tail)
						s.head = tree.ConstFolding.constFolding(s.head);
				assem.InstrList code = funcframe.codegen(stmlist.head);			
				stmlist = stmlist.tail;
				//				System.out.println(funcframe.getName().toString());
				while(stmlist != null){		
					if(printIR){
						irPrinter.prStm(stmlist.head);
						System.out.println("------------------------\n");
					}
					code.append(funcframe.codegen(stmlist.head));
					stmlist = stmlist.tail;
				} 
				regalloc.RegAlloc regmap = new regalloc.RegAlloc();
				code = funcframe.procEntryExit2(code);
				code = funcframe.procEntryExit3(code);
				ArrayList<assem.BasicBlock> basicblocks = assem.BasicBlock.Partition(code);
				if(flowOptSwitch)regalloc.FlowOpt.flowOpt(basicblocks, funcframe);

				if(allocTemp){
					regmap.alloc(basicblocks,28,funcframe,true);
					basicblocks = regmap.getProgram();
				}

				temp.TempMap tempmap;
				if(allocTemp)tempmap = regmap;
				else tempmap = defaultMap;

				if(printAssem){
					out.println(".text");
					for(assem.BasicBlock t : basicblocks){
						code = t.instrs;
						while(code != null){
							out.println(code.head.format(tempmap));
							code = code.tail;
						}
						out.println("\n");
					}
				}
			}
			out.println();
		}

		tree.Stm stm = irTreeWithType.exp.unNx();
		frame.Frame funcframe= topFrame;
		if(printIR)irPrinter.prStm(stm);
		funcframe.procEntryExit1(stm);
		canon.TraceSchedule trace = new canon.TraceSchedule(new canon.BasicBlocks(canon.Canon.linearize(stm)));
		tree.StmList  stmlist = trace.stms;
		if(irOptSwitch)
			for(tree.StmList s = stmlist; s!= null; s = s.tail)
				s.head = tree.ConstFolding.constFolding(s.head);
		assem.InstrList code = funcframe.codegen(stmlist.head);			
		//		System.out.println(funcframe.getName().toString());
		if(printIR)irPrinter.prStm(stmlist.head);
		stmlist = stmlist.tail;
		while(stmlist != null){
			if(printIR)irPrinter.prStm(stmlist.head);
			//			irPrinter.prStm(stmlist.head);
			code.append(funcframe.codegen(stmlist.head));
			stmlist = stmlist.tail;
		}
		regalloc.RegAlloc regmap = new regalloc.RegAlloc();
		code.append(new assem.InstrList(new assem.CallInstr("jal _exit", null, new temp.TempList(funcframe.ZERO(), new temp.TempList(funcframe.SP(), null)), null), null));
		code = funcframe.procEntryExit3(code);
		ArrayList<assem.BasicBlock> basicblocks = assem.BasicBlock.Partition(code);
		if(flowOptSwitch)regalloc.FlowOpt.flowOpt(basicblocks, funcframe);
		if(allocTemp){regmap.alloc(basicblocks,28,topFrame,true);
			basicblocks = regmap.getProgram();
		}
		temp.TempMap tempmap;
		if(allocTemp)tempmap = regmap;
		else tempmap = defaultMap;
		if(printAssem){
			out.println(".text");
			for(ListIterator<assem.BasicBlock> t = basicblocks.listIterator(); t.hasNext();){
				code = t.next().instrs;
				while(code != null){
					out.println(code.head.format(tempmap));
					code = code.tail;
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