package nachos.vm;

import java.util.HashMap;
import java.util.LinkedList;

import nachos.machine.Kernel;
import nachos.machine.Lib;
import nachos.machine.Machine;
import nachos.threads.Lock;
import nachos.userprog.UserKernel;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();
	}

	/**
	 * Initialize this kernel.
	 */
	public void initialize(String[] args) {
		super.initialize(args);
		VMProcess.swapTableLock = new Lock();
		VMProcess.pageTableLock = new Lock();
		//VMProcess.tlbLock = new Lock();
		VMProcess.emptySwapPage = new LinkedList<Integer>();
		VMProcess.globalPageTable = new InvertedPageTable();
		VMProcess.swapTable = new HashMap<VirtualPagePair, Integer>();
		VMProcess.swapFile = VMKernel.fileSystem.open(VMProcess.swapFileName, true);
		Lib.assertTrue(VMProcess.swapFile != null, "Virtual Memory Initialization Failed");
		VMProcess.totalSwapPage = 0;
		VMProcess.tlbSize = Machine.processor().getTLBSize();
	}

	/**
	 * Test this kernel.
	 */
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * Start running user programs.
	 */
	public void run() {
		super.run();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		VMKernel.fileSystem.remove(VMProcess.swapFileName);
		super.terminate();
	}

	private static final char dbgVM = 'v';
}
