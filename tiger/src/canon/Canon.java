package canon;

class MoveCall extends tree.Stm {
  tree.Temp dst;
  tree.Call src;
  MoveCall(tree.Temp d, tree.Call s) {dst=d; src=s;}
  public tree.ExpList kids() {return src.kids();}
  public tree.Stm build(tree.ExpList kids) {
	return new tree.Move(dst, src.build(kids));
  }
}   
  
class ExpCall extends tree.Stm {
  tree.Call call;
  ExpCall(tree.Call c) {call=c;}
  public tree.ExpList kids() {return call.kids();}
  public tree.Stm build(tree.ExpList kids) {
	return new tree.ExpNoValue(call.build(kids));
  }
}   
  
class StmExpList {
  tree.Stm stm;
  tree.ExpList exps;
  StmExpList(tree.Stm s, tree.ExpList e) {stm=s; exps=e;}
}

public class Canon {
  
 static boolean isNop(tree.Stm a) {
   return a instanceof tree.ExpNoValue
          && ((tree.ExpNoValue)a).exp instanceof tree.Const;
 }

 static tree.Stm seq(tree.Stm a, tree.Stm b) {
    if (isNop(a)) return b;
    else if (isNop(b)) return a;
    else return new tree.Seq(a,b);
 }

 static boolean commute(tree.Stm a, tree.Exp b) {
    return isNop(a)
        || b instanceof tree.Name
        || b instanceof tree.Const;
 }

 static tree.Stm do_stm(tree.Seq s) { 
	return seq(do_stm(s.left), do_stm(s.right));
 }

 static tree.Stm do_stm(tree.Move s) { 
	if (s.dst instanceof tree.Temp 
	     && s.src instanceof tree.Call) 
		return reorder_stm(new MoveCall((tree.Temp)s.dst,
						(tree.Call)s.src));
	else if (s.dst instanceof tree.Eseq)
	    return do_stm(new tree.Seq(((tree.Eseq)s.dst).stm,
					new tree.Move(((tree.Eseq)s.dst).exp,
						  s.src)));
	else return reorder_stm(s);
 }

 static tree.Stm do_stm(tree.ExpNoValue s) { 
	if (s.exp instanceof tree.Call)
	       return reorder_stm(new ExpCall((tree.Call)s.exp));
	else return reorder_stm(s);
 }

 static tree.Stm do_stm(tree.Stm s) {
     if (s instanceof tree.Seq) return do_stm((tree.Seq)s);
     else if (s instanceof tree.Move) return do_stm((tree.Move)s);
     else if (s instanceof tree.ExpNoValue) return do_stm((tree.ExpNoValue)s);
     else return reorder_stm(s);
 }

 static tree.Stm reorder_stm(tree.Stm s) {
     StmExpList x = reorder(s.kids());
     return seq(x.stm, s.build(x.exps));
 }

 static tree.Eseq do_exp(tree.Eseq e) {
      tree.Stm stms = do_stm(e.stm);
      tree.Eseq b = do_exp(e.exp);
      return new tree.Eseq(seq(stms,b.stm), b.exp);
  }

 static tree.Eseq do_exp (tree.Exp e) {
       if (e instanceof tree.Eseq) return do_exp((tree.Eseq)e);
       else return reorder_exp(e);
 }
         
 static tree.Eseq reorder_exp (tree.Exp e) {
     StmExpList x = reorder(e.kids());
     return new tree.Eseq(x.stm, e.build(x.exps));
 }

 static StmExpList nopNull = new StmExpList(new tree.ExpNoValue(new tree.Const(0)),null);

 static StmExpList reorder(tree.ExpList exps) {
     if (exps==null) return nopNull;
     else {
       tree.Exp a = exps.head;
       if (a instanceof tree.Call) {
         temp.AtomicTemp t = new temp.AtomicTemp();
	 tree.Exp e = new tree.Eseq(new tree.Move(new tree.Temp(t), a),
				    new tree.Temp(t));
         return reorder(new tree.ExpList(e, exps.tail));
       } else {
	 tree.Eseq aa = do_exp(a);
	 StmExpList bb = reorder(exps.tail);
	 if (commute(bb.stm, aa.exp))
	      return new StmExpList(seq(aa.stm,bb.stm), 
				    new tree.ExpList(aa.exp,bb.exps));
	 else {
	   temp.AtomicTemp t = new temp.AtomicTemp();
	   return new StmExpList(
			  seq(aa.stm, 
			    seq(new tree.Move(new tree.Temp(t),aa.exp),
				 bb.stm)),
			  new tree.ExpList(new tree.Temp(t), bb.exps));
	 }
       }
     }
 }
        
 static tree.StmList linear(tree.Seq s, tree.StmList l) {
      return linear(s.left,linear(s.right,l));
 }
 static tree.StmList linear(tree.Stm s, tree.StmList l) {
    if (s instanceof tree.Seq) return linear((tree.Seq)s, l);
    else return new tree.StmList(s,l);
 }

 static public tree.StmList linearize(tree.Stm s) {
    return linear(do_stm(s), null);
 }
}
