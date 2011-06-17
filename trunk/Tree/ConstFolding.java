package Tree;
public class ConstFolding
{
	public static Exp constFolding(Exp e){
		if(e instanceof BINOP)return constFolding((BINOP)e);
		else if(e instanceof MEM)return constFolding((MEM)e);
		else if(e instanceof ESEQ)return constFolding((ESEQ)e);
		return e;
	}
	public static Exp constFolding(BINOP e){
		e.left = constFolding(e.left);
		e.right = constFolding(e.right);
		if(e.left instanceof CONST && e.right instanceof CONST){
			switch(e.binop){
			case BINOP.PLUS:return new CONST(((CONST)e.left).value + ((CONST)e.right).value);
			case BINOP.MINUS:return new CONST(((CONST)e.left).value - ((CONST)e.right).value);
			case BINOP.MUL:return new CONST(((CONST)e.left).value * ((CONST)e.right).value);
			case BINOP.DIV:
				if(((CONST)e.right).value != 0)
					return new CONST(((CONST)e.left).value / ((CONST)e.right).value);				
			}
		}
		if(e.binop == BINOP.MUL){
			if(e.left instanceof CONST)
				if( ((CONST)e.left).value == 0)return new CONST(0);
			if(e.right instanceof CONST)
				if( ((CONST)e.right).value == 0)return new CONST(0);
			if(e.left instanceof BINOP){
				BINOP left = (BINOP)e.left;
				if(left.binop == BINOP.PLUS || left.binop == BINOP.MINUS){
					return new BINOP(left.binop, constFolding(new BINOP(BINOP.MUL, left.left, e.right)), 
									 constFolding(new BINOP(BINOP.MUL, left.right, e.right)));
				}
			}
			if(e.right instanceof BINOP){
				BINOP right = (BINOP)e.right;
				if(right.binop == BINOP.PLUS || right.binop == BINOP.MINUS){
					return new BINOP(right.binop, constFolding(new BINOP(BINOP.MUL, e.left, right.left)), 
									 constFolding(new BINOP(BINOP.MUL, e.left, right.right)));
				}
			}

		}
		return e;
	}
	public static Exp constFolding(MEM e){
		e.exp = constFolding(e.exp);
		return e;
	}
	public static Exp constFolding(ESEQ e){
		e.stm = constFolding(e.stm);
		e.exp = constFolding(e.exp);
		return e;
	}
	public static Stm constFolding(Stm s){
		if(s instanceof EXP)return constFolding((EXP)s);
		else if (s instanceof CJUMP)return constFolding((CJUMP)s);
		else if (s instanceof MOVE)return constFolding((MOVE)s);
		else if (s instanceof SEQ)return constFolding((SEQ)s);
		return s;
	}
	public static Stm constFolding(EXP s){
		s.exp = constFolding(s.exp);
		return s;
	}
	public static Stm constFolding(CJUMP e){
		e.left = constFolding(e.left);
		e.right = constFolding(e.right);
		if(e.left instanceof CONST && e.right instanceof CONST){
			int left = ((CONST)e.left).value;
			int right = ((CONST)e.right).value;
			switch(e.relop){
			case CJUMP.EQ:if(left == right)return new JUMP(e.iftrue);
				else return new JUMP(e.iffalse);
			case CJUMP.NE:if(left != right)return new JUMP(e.iftrue);
				else return new JUMP(e.iffalse);
			case CJUMP.LT:if(left < right)return new JUMP(e.iftrue);
				else return new JUMP(e.iffalse);
			case CJUMP.LE:if(left <= right)return new JUMP(e.iftrue);
				else return new JUMP(e.iffalse);
			case CJUMP.GT:if(left > right)return new JUMP(e.iftrue);
				else return new JUMP(e.iffalse);
			case CJUMP.GE:if(left >= right)return new JUMP(e.iftrue);
				else return new JUMP(e.iffalse);
			}
		}
		return e;
	}
	public static Stm constFolding(MOVE e){
		e.dst = constFolding(e.dst);
		e.src = constFolding(e.src);
		return e;
	}
	public static Stm constFolding(SEQ e){
		e.left = constFolding(e.left);
		e.right = constFolding(e.right);
		return e;
	}
}