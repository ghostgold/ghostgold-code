package Semant;

class Env
{
	Symbol.Table venv;
	Symbol.Table tenv;
	ErrorMsg.ErrorMsg errorMsg;
	Env(ErrorMsg.ErrorMsg err){errorMsg = err;}
}