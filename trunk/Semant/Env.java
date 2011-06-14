package Semant;

public class Env
{
	public Symbol.Table venv;
	public Symbol.Table tenv;
	ErrorMsg.ErrorMsg errorMsg;
	public Env(ErrorMsg.ErrorMsg err){
		errorMsg = err;
		venv = new Symbol.Table();
		tenv = new Symbol.Table();
	}
	public Env(){
		venv = new Symbol.Table();
		tenv = new Symbol.Table();
	}
}