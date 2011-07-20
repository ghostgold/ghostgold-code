package semant;

public class Env
{
	public symbol.Table venv;
	public symbol.Table tenv;
	errormsg.ErrorMsg errorMsg;
	public Env(errormsg.ErrorMsg err){
		errorMsg = err;
		venv = new symbol.Table();
		tenv = new symbol.Table();
	}
	public Env(){
		venv = new symbol.Table();
		tenv = new symbol.Table();
	}
}