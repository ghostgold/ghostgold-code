package tree;

public class Print {

  java.io.PrintStream out;
  temp.TempMap tmap;

  public Print(java.io.PrintStream o, temp.TempMap t) {out=o; tmap=t;}

  public Print(java.io.PrintStream o) {out=o; tmap=new temp.DefaultMap();}

  void indent(int d) {
      for(int i=0; i<d; i++) 
            out.print(' ');
  }

  void say(String s) {
            out.print(s);
  }

  void sayln(String s) {
	say(s); say("\n");
  }

  void prStm(Seq s, int d) {
      indent(d); sayln("SEQ("); prStm(s.left,d); sayln(",");
      prStm(s.right,d); say(")");
  }

  void prStm(Label s, int d) {
      indent(d); say("LABEL "); say(s.label.toString());
  }

  void prStm(Jump s, int d) {
      indent(d); sayln("JUMP("); prExp(s.exp, d+1); say(")");
  }

  void prStm(Cjump s, int d) {
      indent(d); say("CJUMP("); 
      switch(s.relop) {
        case Cjump.EQ: say("EQ"); break;
        case Cjump.NE: say("NE"); break;
        case Cjump.LT: say("LT"); break;
        case Cjump.GT: say("GT"); break;
        case Cjump.LE: say("LE"); break;
        case Cjump.GE: say("GE"); break;
        case Cjump.ULT: say("ULT"); break;
        case Cjump.ULE: say("ULE"); break;
        case Cjump.UGT: say("UGT"); break;
        case Cjump.UGE: say("UGE"); break;
	default:
         throw new Error("Print.prStm.CJUMP");
       }
       sayln(","); prExp(s.left,d+1); sayln(",");
       prExp(s.right,d+1); sayln(",");
       indent(d+1); say(s.iftrue.toString()); say(","); 
       say(s.iffalse.toString()); say(")");
  }

  void prStm(Move s, int d) {
     indent(d); sayln("MOVE("); prExp(s.dst,d+1); sayln(","); 
           prExp(s.src,d+1); say(")");
  }

  void prStm(ExpNoValue s, int d) {
     indent(d); sayln("EXP("); prExp(s.exp,d+1); say(")"); 
  }

  void prStm(Stm s, int d) {
        if (s instanceof Seq) prStm((Seq)s, d);
   else if (s instanceof Label) prStm((Label)s, d);
   else if (s instanceof Jump) prStm((Jump)s, d);
   else if (s instanceof Cjump) prStm((Cjump)s, d);
   else if (s instanceof Move) prStm((Move)s, d);
   else if (s instanceof ExpNoValue) prStm((ExpNoValue)s, d);
   else throw new Error("Print.prStm");
  }

  void prExp(BinOp e, int d) {
     indent(d); say("BINOP("); 
      switch(e.binop) {
	case BinOp.PLUS: say("PLUS"); break;
	case BinOp.MINUS: say("MINUS"); break;
	case BinOp.MUL: say("MUL"); break;
	case BinOp.DIV: say("DIV"); break;
	case BinOp.AND: say("AND"); break;
	case BinOp.OR: say("OR"); break;
	case BinOp.LSHIFT: say("LSHIFT"); break;
	case BinOp.RSHIFT: say("RSHIFT"); break;
	case BinOp.ARSHIFT: say("ARSHIFT"); break;
	case BinOp.XOR: say("XOR"); break;
	default:
         throw new Error("Print.prExp.BINOP");
       }
      sayln(",");
      prExp(e.left,d+1); sayln(","); prExp(e.right,d+1); say(")");
   }

  void prExp(Mem e, int d) {
     indent(d);
	sayln("MEM("); prExp(e.exp,d+1); say(")");
  }

  void prExp(Temp e, int d) {
     indent(d); say("TEMP "); 
     say(tmap.tempMap(e.temp));
  }

  void prExp(Eseq e, int d) {
     indent(d); sayln("ESEQ("); prStm(e.stm,d+1); sayln(",");
	prExp(e.exp,d+1); say(")");

  }

  void prExp(Name e, int d) {
     indent(d); say("NAME "); say(e.label.toString());
  }

  void prExp(Const e, int d) {
     indent(d); say("CONST "); say(String.valueOf(e.value));
  }

  void prExp(Call e, int d) {
     indent(d); sayln("CALL(");
	prExp(e.func,d+1);
        for(ExpList a = e.args; a!=null; a=a.tail) {
           sayln(","); prExp(a.head,d+2); 
        }
        say(")");
  }

  void prExp(Exp e, int d) {
        if (e instanceof BinOp) prExp((BinOp)e, d);
   else if (e instanceof Mem) prExp((Mem)e, d);
   else if (e instanceof Temp) prExp((Temp)e, d);
   else if (e instanceof Eseq) prExp((Eseq)e, d);
   else if (e instanceof Name) prExp((Name)e, d);
   else if (e instanceof Const) prExp((Const)e, d);
   else if (e instanceof Call) prExp((Call)e, d);
   else throw new Error("Print.prExp");
  }

  public void prStm(Stm s) {prStm(s,0); say("\n");}
  public void prExp(Exp e) {prExp(e,0); say("\n");}

}
