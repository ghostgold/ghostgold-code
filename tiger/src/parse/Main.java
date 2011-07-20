package parse;

public class Main {

	public static void parsermain(String argv[])
		throws java.io.IOException, java.lang.Exception{
		String filename = argv[0];
		errormsg.ErrorMsg errorSender= new errormsg.ErrorMsg(filename);
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
		absyn.Exp exp = (absyn.Exp)(parseTree.value);
		absyn.Print printer = new absyn.Print(System.out);
		printer.prExp(exp,4);
		return ;
	}

}


