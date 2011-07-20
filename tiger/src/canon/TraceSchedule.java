package canon;

public class TraceSchedule {

  public tree.StmList stms;
  BasicBlocks theBlocks;
  java.util.Dictionary table = new java.util.Hashtable();

  tree.StmList getLast(tree.StmList block) {
     tree.StmList l=block;
     while (l.tail.tail!=null)  l=l.tail;
     return l;
  }

  void trace(tree.StmList l) {
   for(;;) {
     tree.Label lab = (tree.Label)l.head;
     table.remove(lab.label);
     tree.StmList last = getLast(l);
     tree.Stm s = last.tail.head;
     if (s instanceof tree.Jump) {
	tree.Jump j = (tree.Jump)s;
        tree.StmList target = (tree.StmList)table.get(j.targets.head);
	if (j.targets.tail==null && target!=null) {
               last.tail=target;
	       l=target;
        }
	else {
	  last.tail.tail=getNext();
	  return;
        }
     }
     else if (s instanceof tree.Cjump) {
	tree.Cjump j = (tree.Cjump)s;
        tree.StmList t = (tree.StmList)table.get(j.iftrue);
        tree.StmList f = (tree.StmList)table.get(j.iffalse);
        if (f!=null) {
	  last.tail.tail=f; 
	  l=f;
	}
        else if (t!=null) {
	  last.tail.head=new tree.Cjump(tree.Cjump.notRel(j.relop),
					j.left,j.right,
					j.iffalse,j.iftrue);
	  last.tail.tail=t;
	  l=t;
        }
        else {
	  temp.AtomicLabel ff = new temp.AtomicLabel();
	  last.tail.head=new tree.Cjump(j.relop,j.left,j.right,
					j.iftrue,ff);
	  last.tail.tail=new tree.StmList(new tree.Label(ff),
		           new tree.StmList(new tree.Jump(j.iffalse),
					    getNext()));
	  return;
        }
     }
     else throw new Error("Bad basic block in TraceSchedule");
    }
  }

  tree.StmList getNext() {
      if (theBlocks.blocks==null) 
	return new tree.StmList(new tree.Label(theBlocks.done), null);
      else {
	 tree.StmList s = theBlocks.blocks.head;
	 tree.Label lab = (tree.Label)s.head;
	 if (table.get(lab.label) != null) {
          trace(s);
	  return s;
         }
         else {
	   theBlocks.blocks = theBlocks.blocks.tail;
           return getNext();
         }
      }
  }

  public TraceSchedule(BasicBlocks b) {
    theBlocks=b;
    for(StmListList l = b.blocks; l!=null; l=l.tail)
       table.put(((tree.Label)l.head.head).label, l.head);
    stms=getNext();
    table=null;
  }        
}


