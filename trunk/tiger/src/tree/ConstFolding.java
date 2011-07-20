package tree;
public class ConstFolding
{
	public static Exp constFolding(Exp e){
		if(e instanceof BinOp)return constFolding((BinOp)e);
		else if(e instanceof Mem)return constFolding((Mem)e);
		else if(e instanceof Eseq)return constFolding((Eseq)e);
		return e;
	}
	public static Exp constFolding(BinOp e){
		e.left = constFolding(e.left);
		e.right = constFolding(e.right);
		if(e.left instanceof Const && e.right instanceof Const){
			switch(e.binop){
			case BinOp.PLUS:return new Const(((Const)e.left).value + ((Const)e.right).value);
			case BinOp.MINUS:return new Const(((Const)e.left).value - ((Const)e.right).value);
			case BinOp.MUL:return new Const(((Const)e.left).value * ((Const)e.right).value);
			case BinOp.DIV:
				if(((Const)e.right).value != 0)
					return new Const(((Const)e.left).value / ((Const)e.right).value);				
			}
		}
		if(e.binop == BinOp.PLUS){
			if(e.left instanceof BinOp){
				BinOp left = (BinOp)e.left;
				if(left.binop == BinOp.PLUS ){
					if(left.left instanceof Const)
						return new BinOp(BinOp.PLUS, left.left, new BinOp(BinOp.PLUS, left.right, e.right));
					if(left.right instanceof Const)
						return new BinOp(BinOp.PLUS, left.right, new BinOp(BinOp.PLUS, left.left, e.right));
				}
				if(left.binop == BinOp.MINUS){
					if(left.right instanceof Const)
						return new BinOp(BinOp.MINUS, new BinOp(BinOp.PLUS, left.left, e.right), left.right);
				}
			}
			else if(e.right instanceof BinOp){
				BinOp right = (BinOp)e.right;
				if(right.binop == BinOp.PLUS){
					if(right.left instanceof Const)
						return new BinOp(BinOp.PLUS, new BinOp(BinOp.PLUS, e.left, right.right), right.left);
					if(right.right instanceof Const)
						return new BinOp(BinOp.PLUS, new BinOp(BinOp.PLUS, e.left, right.left), right.right);
				}
				if(right.binop == BinOp.MINUS){
					if(right.left instanceof Const)
						return new BinOp(BinOp.PLUS, new BinOp(BinOp.MINUS, e.left, right.right), right.left);
					if(right.right instanceof Const)
						return new BinOp(BinOp.MINUS, new BinOp(BinOp.PLUS, e.left, right.left), right.right);
				}
			}
		}
		if(e.binop == BinOp.MUL){
			if(e.left instanceof Const)
				if( ((Const)e.left).value == 0)return new Const(0);
			if(e.right instanceof Const)
				if( ((Const)e.right).value == 0)return new Const(0);
			if(e.left instanceof BinOp){
				BinOp left = (BinOp)e.left;
				if(left.binop == BinOp.PLUS || left.binop == BinOp.MINUS){
					return new BinOp(left.binop, constFolding(new BinOp(BinOp.MUL, left.left, e.right)), 
									 constFolding(new BinOp(BinOp.MUL, left.right, e.right)));
				}
			}
			if(e.right instanceof BinOp){
				BinOp right = (BinOp)e.right;
				if(right.binop == BinOp.PLUS || right.binop == BinOp.MINUS){
					return new BinOp(right.binop, constFolding(new BinOp(BinOp.MUL, e.left, right.left)), 
									 constFolding(new BinOp(BinOp.MUL, e.left, right.right)));
				}
			}

		}
		return e;
	}
	public static Exp constFolding(Mem e){
		e.exp = constFolding(e.exp);
		return e;
	}
	public static Exp constFolding(Eseq e){
		e.stm = constFolding(e.stm);
		e.exp = constFolding(e.exp);
		return e;
	}
	public static Stm constFolding(Stm s){
		if(s instanceof ExpNoValue)return constFolding((ExpNoValue)s);
		else if (s instanceof Cjump)return constFolding((Cjump)s);
		else if (s instanceof Move)return constFolding((Move)s);
		else if (s instanceof Seq)return constFolding((Seq)s);
		return s;
	}
	public static Stm constFolding(ExpNoValue s){
		s.exp = constFolding(s.exp);
		return s;
	}
	public static Stm constFolding(Cjump e){
		e.left = constFolding(e.left);
		e.right = constFolding(e.right);
		if(e.left instanceof Const && e.right instanceof Const){
			int left = ((Const)e.left).value;
			int right = ((Const)e.right).value;
			switch(e.relop){
			case Cjump.EQ:if(left == right)return new Jump(e.iftrue);
				else return new Jump(e.iffalse);
			case Cjump.NE:if(left != right)return new Jump(e.iftrue);
				else return new Jump(e.iffalse);
			case Cjump.LT:if(left < right)return new Jump(e.iftrue);
				else return new Jump(e.iffalse);
			case Cjump.LE:if(left <= right)return new Jump(e.iftrue);
				else return new Jump(e.iffalse);
			case Cjump.GT:if(left > right)return new Jump(e.iftrue);
				else return new Jump(e.iffalse);
			case Cjump.GE:if(left >= right)return new Jump(e.iftrue);
				else return new Jump(e.iffalse);
			}
		}
		return e;
	}
	public static Stm constFolding(Move e){
		e.dst = constFolding(e.dst);
		e.src = constFolding(e.src);
		return e;
	}
	public static Stm constFolding(Seq e){
		e.left = constFolding(e.left);
		e.right = constFolding(e.right);
		return e;
	}
}