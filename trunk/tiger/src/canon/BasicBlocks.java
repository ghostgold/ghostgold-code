package canon;

public class BasicBlocks {
  public StmListList blocks;
  public temp.AtomicLabel done;

  private StmListList lastBlock;
  private tree.StmList lastStm;

  private void addStm(tree.Stm s) {
	lastStm = lastStm.tail = new tree.StmList(s,null);
  }

  private void doStms(tree.StmList l) {
      if (l==null) 
	doStms(new tree.StmList(new tree.Jump(done), null));
      else if (l.head instanceof tree.Jump 
	      || l.head instanceof tree.Cjump) {
	addStm(l.head);
	mkBlocks(l.tail);
      } 
      else if (l.head instanceof tree.Label)
           doStms(new tree.StmList(new tree.Jump(((tree.Label)l.head).label), 
	  			   l));
      else {
	addStm(l.head);
	doStms(l.tail);
      }
  }

  void mkBlocks(tree.StmList l) {
     if (l==null) return;
     else if (l.head instanceof tree.Label) {
	lastStm = new tree.StmList(l.head,null);
        if (lastBlock==null)
  	   lastBlock= blocks= new StmListList(lastStm,null);
        else
  	   lastBlock = lastBlock.tail = new StmListList(lastStm,null);
	doStms(l.tail);
     }
     else mkBlocks(new tree.StmList(new tree.Label(new temp.AtomicLabel()), l));
  }
   

  public BasicBlocks(tree.StmList stms) {
    done = new temp.AtomicLabel();
    mkBlocks(stms);
  }
}
