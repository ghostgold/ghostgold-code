package nachos.vm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import nachos.machine.CoffSection;
import nachos.machine.Lib;
import nachos.machine.Machine;
import nachos.machine.OpenFile;
import nachos.machine.Processor;
import nachos.machine.TranslationEntry;
import nachos.threads.Lock;
import nachos.userprog.UserProcess;

class VirtualPagePair {
	public VirtualPagePair(int page, int id) {
		pageNum = page;
		pid = id;
	}
	int pageNum;
	int pid;
	@Override
	public boolean equals(Object o) {
		if (o instanceof VirtualPagePair) {
			return (((VirtualPagePair)o).pageNum == pageNum) && (((VirtualPagePair)o).pid == pid);  
		}
		return false;
	}
	@Override 
	public int hashCode() {
		return (pageNum << 16) | (pid & 0xFFFF);
	}
}

class InvertedPageTable {
	public InvertedPageTable() {
		hashTable = new HashMap<VirtualPagePair, TranslationEntry>();
		pageTable = new VirtualPagePair[Machine.processor().getNumPhysPages()];
		point = 0;
	}
	Map<VirtualPagePair, TranslationEntry> hashTable;
	VirtualPagePair[] pageTable;
	int point;
	
	public void insert(VirtualPagePair p, TranslationEntry t) {
		//Lib.debug(VMProcess.dbgVM, "map (" + p.pageNum+ "-" + p.pid + ") to " + t.ppn + "\n");					
		if (pageTable[t.ppn] != null)
			hashTable.remove(pageTable[t.ppn]);
		pageTable[t.ppn] = p;
		hashTable.put(p, t);
	}
	
	public void remove(int ppn) {
		
		if (pageTable[ppn] != null) {
			//Lib.debug(VMProcess.dbgVM, "unmap (" + pageTable[ppn].pageNum+ "-" + pageTable[ppn].pid + ") to " + ppn + "\n");								
			hashTable.remove(pageTable[ppn]);
			pageTable[ppn] = null;
		}
	}
	public TranslationEntry getPhyPage(VirtualPagePair p) {
		return hashTable.get(p);
	}
	
	public VirtualPagePair getVirtualPage(int ppn) {
		return pageTable[ppn];
	}
	
	public int getFreePage() {
		for (int i = 0; i < Machine.processor().getNumPhysPages(); i++) {
			if (pageTable[i] == null) 
				return i;
		}
		return -1;
	}
	
	public VirtualPagePair getPageToBeSwapped() {
		dump();
		for (int i = 0; i < VMProcess.tlbSize; i++) {
			TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
			if (tlbEntry.valid) {
				insert(new VirtualPagePair(tlbEntry.vpn,VMKernel.currentProcess().getPid()), tlbEntry);
			}
		}
//		while (true) {
//			//Lib.assertTrue(pageTable[point] == null);
//			point %= Machine.processor().getNumPhysPages();
//			if (pageTable[point] != null) {
//				VirtualPagePair vpp = pageTable[point];
//				if (hashTable.get(vpp).used == false) {
//					point++;
//					return vpp;
//				}
//				else 
//					hashTable.get(vpp).used = false;
//				point++;
//			}
//		}
		return pageTable[Lib.random(Machine.processor().getNumPhysPages())];
	}
	
	void dump() {
		Lib.debug(VMProcess.dbgVM, "pageTable");
		for (int i = 0; i < pageTable.length; i++) {
			String message = "no entry";
			if (pageTable[i] != null)
				message = pageTable[i].pageNum + "," + pageTable[i].pid + "->" + hashTable.get(pageTable[i]).ppn;
			Lib.debug(VMProcess.dbgVM, message);
		}
		Lib.debug(VMProcess.dbgVM, "hashTable size = " + hashTable.size() + "\n");
	}
}
/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
	/**
	 * Allocate a new process.
	 */
	public VMProcess() {
		super();
		tlb = new TranslationEntry[tlbSize];
		for (int i = 0; i < tlbSize; i++) {
			tlb[i] = new TranslationEntry();
		}
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
		super.saveState();
		for (int i = 0; i < tlbSize; i++) {
			TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
			tlb[i] = new TranslationEntry(tlbEntry);
			if (tlbEntry.valid) {
				globalPageTable.insert(new VirtualPagePair(tlbEntry.vpn, pid), tlbEntry);
			}
		}
		Lib.debug(dbgVM, "Context switch " + pid);
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		for (int i = 0; i < tlbSize; i++) {
			if (tlb[i].valid && globalPageTable.getPhyPage(new VirtualPagePair(tlb[i].vpn, pid)) != null) {
				Machine.processor().writeTLBEntry(i, globalPageTable.getPhyPage(new VirtualPagePair(tlb[i].vpn, pid)));
			}
			else 
				Machine.processor().writeTLBEntry(i, new TranslationEntry());
		}		
	}

	private int getFreeTLB() {
		for (int i = 0; i < tlbSize; i++) {
			if (Machine.processor().readTLBEntry(i).valid == false)
				return i;
		}
		return Lib.random(tlbSize);
	}
	
	private void updateTLB(int i, TranslationEntry t) {
		Processor processor = Machine.processor();
		TranslationEntry oldEntry = processor.readTLBEntry(i);
		if (oldEntry.valid) 
			globalPageTable.insert(new VirtualPagePair(oldEntry.vpn, pid), oldEntry);
		processor.writeTLBEntry(i, t);
	}
	@Override 
	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);
		byte[] memory = Machine.processor().getMemory();
		pageTableLock.acquire();
		int totalAmount = 0;
		while (true) {
			int vpn = Processor.pageFromAddress(vaddr);
			if (vpn >= numPages)
				break;
			
			int pageOffset = Processor.offsetFromAddress(vaddr);
			int amount = Math.min(pageSize - pageOffset, length);

			TranslationEntry translation = translate(vpn);
			translation.used = true;
			updateTLB(getFreeTLB(), translation);
			int ppn = translation.ppn;
			int paddr = ppn * pageSize + pageOffset;
			System.arraycopy(memory, paddr, data, offset, amount);

			length -= amount;
			offset += amount;
			totalAmount += amount;
			vaddr += amount;
			if (length == 0)
				break;
		}
		pageTableLock.release();		
		return totalAmount;		
	}
	
	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();
		pageTableLock.acquire();
		int totalAmount = 0;
		while (true) {
			int vpn = Processor.pageFromAddress(vaddr);
			if (vpn >= numPages)
				break;
			TranslationEntry translation = translate(vpn);
			if (translation.readOnly)
				break;
			translation.used = true;
			translation.dirty = true;
			if (VMKernel.currentProcess().getPid() == pid) {
				for (int i = 0; i < tlbSize; i++) {
					if (Machine.processor().readTLBEntry(i).ppn == translation.ppn) {
						Machine.processor().writeTLBEntry(i, new TranslationEntry());
					}
				}
			}
			int pageOffset = Processor.offsetFromAddress(vaddr);
			int amount = Math.min(pageSize - pageOffset, length);
			int ppn = translation.ppn;
			int paddr = ppn * pageSize + pageOffset;
			System.arraycopy(data, offset, memory, paddr, amount);
			length -= amount;
			offset += amount;
			totalAmount += amount;
			vaddr += amount;
			if(length == 0)
				break;
		}
		pageTableLock.release();			
		return totalAmount;
	}
	
	/**
	 * Initializes page tables for this process so that the executable can be
	 * demand-paged.
	 * 
	 * @return <tt>true</tt> if successful.
	 */
	protected boolean loadSections() {
		//return super.loadSections();
		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	@Override
	protected void unloadSections() {
		pageTableLock.acquire();
		//swapTableLock.acquire();
		for (int i = 0; i < numPages; i++) {
			VirtualPagePair query = new VirtualPagePair(i, pid);
			TranslationEntry translation = globalPageTable.getPhyPage(query);
			if (translation != null) 
				globalPageTable.remove(translation.ppn);
			Integer swapPage = swapTable.get(query);
			if (swapPage != null) {
				emptySwapPage.add(swapPage);
				swapTable.remove(query);
			}
		}	
		for (int i = 0; i < tlbSize; i++) {
			Machine.processor().writeTLBEntry(i, new TranslationEntry());
		}
		//swapTableLock.release();
		pageTableLock.release();
		for (int i = 0; i < maxFileOpened; i++) 
			if (fileTable[i] != null)
				fileTable[i].close();
		coff.close();		
	}

	/**
	 * get the translation entry for virtual page
	 * @param pageNum
	 * @param id
	 * process id
	 * @return
	 */
	private TranslationEntry translate(int pageNum) {
		//pageTableLock.acquire();
		Lib.assertTrue(pageTableLock.isHeldByCurrentThread());
		VirtualPagePair query = new VirtualPagePair(pageNum, pid); 
		TranslationEntry result = globalPageTable.getPhyPage(query);
		if (result == null){
			numberOfPageFault++;
			swapTableLock.acquire();			
			int ppn = getFreePage();
			result = new TranslationEntry(pageNum, ppn, true, false, false, false);
			globalPageTable.insert(query, result);
			
			Integer swapPos = swapTable.get(query);
			if (swapPos != null) { // in swap file
				swapIn(ppn, swapPos);
			}
			else {// new stack file or coff section
				if (pageNum < numPages - stackPages - 1) { // page in coff file, lazy load
					loadPage(ppn, pageNum, result);
				}
			}
			swapTableLock.release();
		}
		//pageTableLock.release();
		return result;
	}
	
	private int getFreePage() {
		int ppn = globalPageTable.getFreePage();
		if (ppn != -1)
			return ppn;
		VirtualPagePair vpp = globalPageTable.getPageToBeSwapped();
		TranslationEntry translation = globalPageTable.getPhyPage(vpp);
		for (int i = 0; i < tlbSize; i++) {
			if (Machine.processor().readTLBEntry(i).valid && Machine.processor().readTLBEntry(i).ppn == translation.ppn) {
				Machine.processor().writeTLBEntry(i, new TranslationEntry());
			}
		}
		if (translation.dirty) {
			Integer dest = swapTable.get(vpp);
			if (dest == null) {
				dest = getFreeSwapPage();
				swapTable.put(vpp, dest);
			}
			swapOut(translation.ppn, dest.intValue());
		}
		globalPageTable.remove(translation.ppn);		
		return translation.ppn;
	}
	
	private int getFreeSwapPage() {
		if (emptySwapPage.isEmpty())
			return new Integer(totalSwapPage++);
		else 
			return emptySwapPage.remove();
	}
	
	private void swapOut(int ppn, int spn) {
		byte[] memory = Machine.processor().getMemory();
		int count = swapFile.write(spn * pageSize, memory, ppn * pageSize, pageSize);
		Lib.assertTrue(count == pageSize);
		
	}
	
	private void loadPage(int ppn, int pageNum, TranslationEntry result) {
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
			if (pageNum >= section.getFirstVPN() && pageNum < section.getFirstVPN() + section.getLength()) {
				section.loadPage(pageNum - section.getFirstVPN(), ppn);
				if (section.isReadOnly())
					result.readOnly = true;
				return;
			}
		}
	}
	
	private void swapIn(int ppn, int spn) {
		byte[] data = new byte[pageSize];
		int count = swapFile.read(spn * pageSize, data, 0, pageSize);
		Lib.assertTrue(count == pageSize);
		System.arraycopy(data, 0, Machine.processor().getMemory(), ppn * pageSize, pageSize);
	}
	@Override
	protected int handleHalt() {
		VMKernel.fileSystem.remove(swapFileName);
		Machine.halt();
		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;

	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
	 * . The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause
	 *            the user exception that occurred.
	 */
	

	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
		case Processor.exceptionTLBMiss:
			int vaddr = processor.readRegister(Processor.regBadVAddr);
			if (Processor.pageFromAddress(vaddr) < 0 || Processor.pageFromAddress(vaddr) >= numPages) {
				super.handleException(Processor.exceptionAddressError);
			}
			pageTableLock.acquire();
			TranslationEntry newEntry = translate(Processor.pageFromAddress(vaddr));
			newEntry.used = true;
			updateTLB(getFreeTLB(), newEntry);
//			int choice = getFreeTLB();
//			TranslationEntry oldEntry = processor.readTLBEntry(choice);
//			if (oldEntry.valid) 
//				globalPageTable.insert(new VirtualPagePair(oldEntry.ppn, pid), oldEntry);
//			processor.writeTLBEntry(choice, newEntry);
			pageTableLock.release();
			break;
		
		default:
			super.handleException(cause);
			break;
		}
	}

	private static final int pageSize = Processor.pageSize;
	static int tlbSize;
	private static final char dbgProcess = 'a';
	static final char dbgVM = 'v';
	private TranslationEntry[] tlb;
	static Lock pageTableLock;
	public static int numberOfPageFault = 0;
	static Lock swapTableLock;
	//static Lock tlbLock;
	static InvertedPageTable globalPageTable;
	static Map<VirtualPagePair, Integer> swapTable;
	static OpenFile swapFile;
	
	static LinkedList<Integer> emptySwapPage;
	static int totalSwapPage;
	static String swapFileName = "swap";
}
