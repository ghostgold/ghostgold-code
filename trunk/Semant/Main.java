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
		Semant semant = new Semant(errorSender);
		semant.transProg(exp);
		if(semant.semantError){
			System.exit(1);
		}
		return ;
	}

}


