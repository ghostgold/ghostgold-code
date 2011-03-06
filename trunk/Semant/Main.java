package Semant;
import Parse.*;
public class Main {

	public static void main(String argv[])
		throws java.io.IOException, java.lang.Exception{
		String filename = argv[0];
		ErrorMsg.ErrorMsg errorSender= new ErrorMsg.ErrorMsg(filename);
		java.io.InputStream inp=new java.io.FileInputStream(filename);
		
		Lexer scanner = new Lexer(inp,errorSender);
		Parser par =new Parser(scanner);
		java_cup.runtime.Symbol parseTree;
		try{
			parseTree = (par.parse());
		}
		catch(Exception e)
			{
				System.out.println("error");
				return ;
			}
		Absyn.Exp exp = (Absyn.Exp)(parseTree.value);
		Absyn.Print printer = new Absyn.Print(System.out);
		printer.prExp(exp,4);
		System.out.print("\n");
		Frame.Frame topFrame = new Mips.MipsFrame(null, null);
		Translate.Level top = new Translate.Level(topFrame);
		Semant semant = new Semant(errorSender, top);
		semant.init();
		ExpTy total = semant.transExp(exp, null);
		Tree.Print irPrinter = new Tree.Print(System.out);
		for(Translate.Frag f = semant.translate.getResults(); f != null; f = f.next ){
			if(f instanceof Translate.DataFrag){
				System.out.println(((Translate.DataFrag)f).data);
			}
			else irPrinter.prStm(((Translate.ProcFrag)f).body);
			System.out.println();
		}
		irPrinter.prStm(total.exp.unNx());
		if(semant.semantError){
			System.exit(1);
		}
		return;
	}

}


