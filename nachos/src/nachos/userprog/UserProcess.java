package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import java.util.ArrayList;
import java.io.EOFException;

/**
 * Encapsulates the state of a user process that is not contained in its user
 * thread (or threads). This includes its address translation state, a file
 * table, and information about the program being executed.
 * 
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 * 
 * @see nachos.vm.VMProcess
 * @see nachos.network.NetProcess
 */
public class UserProcess {
	/**
	 * Allocate a new process.
	 */
	public UserProcess() {
//		int numPhysPages = Machine.processor().getNumPhysPages();
//		pageTable = new TranslationEntry[numPhysPages];
//		for (int i = 0; i < numPhysPages; i++)
//			pageTable[i] = new TranslationEntry(i, i, true, false, false, false);
		
		fileTable[0] = UserKernel.console.openForReading();
		fileTable[1] = UserKernel.console.openForWriting();
	}
	/**
	 * Allocate and return a new process of the correct class. The class name is
	 * specified by the <tt>nachos.conf</tt> key
	 * <tt>Kernel.processClassName</tt>.
	 * 
	 * @return a new process of the correct class.
	 */
	public static UserProcess newUserProcess() {
		return (UserProcess) Lib.constructObject(Machine.getProcessClassName());
	}

	/**
	 * Execute the specified program with the specified arguments. Attempts to
	 * load the program, and then forks a thread to run it.
	 * 
	 * @param name
	 *            the name of the file containing the executable.
	 * @param args
	 *            the arguments to pass to the executable.
	 * @return <tt>true</tt> if the program was successfully executed.
	 */
	public boolean execute(String name, String[] args) {
		if (!load(name, args))
			return false;
		
		numRunning++;
		new UThread(this).setName(name).fork();
		return true;
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		Machine.processor().setPageTable(pageTable);
	}

	/**
	 * Read a null-terminated string from this process's virtual memory. Read at
	 * most <tt>maxLength + 1</tt> bytes from the specified address, search for
	 * the null terminator, and convert it to a <tt>java.lang.String</tt>,
	 * without including the null terminator. If no null terminator is found,
	 * returns <tt>null</tt>.
	 * 
	 * @param vaddr
	 *            the starting virtual address of the null-terminated string.
	 * @param maxLength
	 *            the maximum number of characters in the string, not including
	 *            the null terminator.
	 * @return the string read, or <tt>null</tt> if no null terminator was
	 *         found.
	 */
	public String readVirtualMemoryString(int vaddr, int maxLength) {
		Lib.assertTrue(maxLength >= 0);

		byte[] bytes = new byte[maxLength + 1];

		int bytesRead = readVirtualMemory(vaddr, bytes);

		for (int length = 0; length < bytesRead; length++) {
			if (bytes[length] == 0)
				return new String(bytes, 0, length);
		}

		return null;
	}

	/**
	 * Transfer data from this process's virtual memory to all of the specified
	 * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 * 
	 * @param vaddr
	 *            the first byte of virtual memory to read.
	 * @param data
	 *            the array where the data will be stored.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data) {
		return readVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from this process's virtual memory to the specified array.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no data
	 * could be copied).
	 * 
	 * @param vaddr
	 *            the first byte of virtual memory to read.
	 * @param data
	 *            the array where the data will be stored.
	 * @param offset
	 *            the first byte to write in the array.
	 * @param length
	 *            the number of bytes to transfer from virtual memory to the
	 *            array.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		// for now, just assume that virtual addresses equal physical addresses
//		if (vaddr < 0 || vaddr >= memory.length)
//			return 0;

//		int amount = Math.min(length, memory.length - vaddr);
//		System.arraycopy(memory, vaddr, data, offset, amount);

		int totalAmount = 0;
		while (true) {
			int vpn = Processor.pageFromAddress(vaddr);
			if (vpn >= pageTable.length)
				break;
			
			int pageOffset = Processor.offsetFromAddress(vaddr);
			int amount = Math.min(pageSize - pageOffset, length);
			int ppn = pageTable[vpn].ppn;
			int paddr = ppn * pageSize + pageOffset;
			System.arraycopy(memory, paddr, data, offset, amount);
			length -= amount;
			offset += amount;
			totalAmount += amount;
			vaddr += amount;
			if (length == 0)
				break;
		}
		return totalAmount;
	}

	/**
	 * Transfer all data from the specified array to this process's virtual
	 * memory. Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 * 
	 * @param vaddr
	 *            the first byte of virtual memory to write.
	 * @param data
	 *            the array containing the data to transfer.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data) {
		return writeVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from the specified array to this process's virtual memory.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no data
	 * could be copied).
	 * 
	 * @param vaddr
	 *            the first byte of virtual memory to write.
	 * @param data
	 *            the array containing the data to transfer.
	 * @param offset
	 *            the first byte to transfer from the array.
	 * @param length
	 *            the number of bytes to transfer from the array to virtual
	 *            memory.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		// for now, just assume that virtual addresses equal physical addresses
//		if (vaddr < 0 || vaddr >= memory.length)
//			return 0;
//
//		int amount = Math.min(length, memory.length - vaddr);
//		System.arraycopy(data, offset, memory, vaddr, amount);
		int totalAmount = 0;
		while (true) {
			int vpn = Processor.pageFromAddress(vaddr);
			if (vpn >= pageTable.length)
				break;
			if (pageTable[vpn].readOnly)
				break;
			int pageOffset = Processor.offsetFromAddress(vaddr);
			int amount = Math.min(pageSize - pageOffset, length);
			int ppn = pageTable[vpn].ppn;
			int paddr = ppn * pageSize + pageOffset;
			System.arraycopy(data, offset, memory, paddr, amount);
			length -= amount;
			offset += amount;
			totalAmount += amount;
			vaddr += amount;
			if(length == 0)
				break;
		}
		return totalAmount;
	}

	
	/**
	 * fill page table when a process is created
	 * @param physicalPages
	 * 		    the array containing the allocated physical pages number 
	 */
	boolean fillPageTable(int[] physicalPages) {
		try{
			for (int i = 0; i < physicalPages.length; i++) 
				pageTable[i] = new TranslationEntry(i,physicalPages[i],true, false, false, false);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Load the executable with the specified name into this process, and
	 * prepare to pass it the specified arguments. Opens the executable, reads
	 * its header information, and copies sections and arguments into this
	 * process's virtual memory.
	 * 
	 * @param name
	 *            the name of the file containing the executable.
	 * @param args
	 *            the arguments to pass to the executable.
	 * @return <tt>true</tt> if the executable was successfully loaded.
	 */
	private boolean load(String name, String[] args) {
		Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

		OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
		if (executable == null) {
			Lib.debug(dbgProcess, "\topen failed");
			return false;
		}

		try {
			coff = new Coff(executable);
		} catch (EOFException e) {
			executable.close();
			Lib.debug(dbgProcess, "\tcoff load failed");
			return false;
		}

		// make sure the sections are contiguous and start at page 0
		numPages = 0;
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
			if (section.getFirstVPN() != numPages) {
				coff.close();
				Lib.debug(dbgProcess, "\tfragmented executable");
				return false;
			}
			numPages += section.getLength();
		}

		// make sure the argv array will fit in one page
		byte[][] argv = new byte[args.length][];
		int argsSize = 0;
		for (int i = 0; i < args.length; i++) {
			argv[i] = args[i].getBytes();
			// 4 bytes for argv[] pointer; then string plus one for null byte
			argsSize += 4 + argv[i].length + 1;
		}
		if (argsSize > pageSize) {
			coff.close();
			Lib.debug(dbgProcess, "\targuments too long");
			return false;
		}

		// program counter initially points at the program entry point
		initialPC = coff.getEntryPoint();

		// next comes the stack; stack pointer initially points to top of it
		numPages += stackPages;
		initialSP = numPages * pageSize;

		// and finally reserve 1 page for arguments
		numPages++;


		if (!loadSections()) {
			coff.close();
			return false;
		}

		// store arguments in last page
		int entryOffset = (numPages - 1) * pageSize;
		int stringOffset = entryOffset + args.length * 4;

		this.argc = args.length;
		this.argv = entryOffset;

		for (int i = 0; i < argv.length; i++) {
			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
			Lib
					.assertTrue(writeVirtualMemory(entryOffset,
							stringOffsetBytes) == 4);
			entryOffset += 4;
			Lib
					.assertTrue(writeVirtualMemory(stringOffset, argv[i]) == argv[i].length);
			stringOffset += argv[i].length;
			Lib
					.assertTrue(writeVirtualMemory(stringOffset,
							new byte[] { 0 }) == 1);
			stringOffset += 1;
		}

		return true;
	}

	/**
	 * Allocates memory for this process, and loads the COFF sections into
	 * memory. If this returns successfully, the process will definitely be run
	 * (this is the last step in process initialization that can fail).
	 * 
	 * @return <tt>true</tt> if the sections were successfully loaded.
	 */
	protected boolean loadSections() {
		if (numPages > Machine.processor().getNumPhysPages()) {
			coff.close();
			Lib.debug(dbgProcess, "\tinsufficient physical memory");
			return false;
		}
		
		pageTable = new TranslationEntry[numPages];
		int[] physicalPages = UserKernel.allocPages(numPages);
		if(physicalPages == null) {
			return false;
		}
		if (!fillPageTable(physicalPages)) {
			return false;
		}
		// load sections
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);

			Lib.debug(dbgProcess, "\tinitializing " + section.getName()
					+ " section (" + section.getLength() + " pages)");

			for (int i = 0; i < section.getLength(); i++) {
				int vpn = section.getFirstVPN() + i;
				int ppn = pageTable[vpn].ppn;
				// for now, just assume virtual addresses=physical addresses
				section.loadPage(i, ppn);
				if (section.isReadOnly()) 
					pageTable[vpn].readOnly = true;
			}
		}

		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		int[] pageUsed = new int[pageTable.length];
		for (int i = 0; i < pageUsed.length; i++) 
			pageUsed[i] = pageTable[i].ppn;
		UserKernel.releasePages(pageUsed);
		for (int i = 0; i < maxFileOpened; i++) 
			if (fileTable[i] != null)
				fileTable[i].close();
		coff.close();
	}

	/**
	 * Initialize the processor's registers in preparation for running the
	 * program loaded into this process. Set the PC register to point at the
	 * start function, set the stack pointer register to point at the top of the
	 * stack, set the A0 and A1 registers to argc and argv, respectively, and
	 * initialize all other registers to 0.
	 */
	public void initRegisters() {
		Processor processor = Machine.processor();

		// by default, everything's 0
		for (int i = 0; i < Processor.numUserRegisters; i++)
			processor.writeRegister(i, 0);

		// initialize PC and SP according
		processor.writeRegister(Processor.regPC, initialPC);
		processor.writeRegister(Processor.regSP, initialSP);

		// initialize the first two argument registers to argc and argv
		processor.writeRegister(Processor.regA0, argc);
		processor.writeRegister(Processor.regA1, argv);
	}

	/**
	 * Handle the halt() system call.
	 */
	protected int handleHalt() {

		Machine.halt();

		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;
	}

	void handleExit() {
		try {
			unloadSections();
			numRunning--;			
			if (numRunning == 0) {
				Kernel.kernel.terminate();
			}			
			UThread.finish();
		} catch (Exception e) {
			return;
		}
	}
	
	private static final int syscallHalt = 0, syscallExit = 1, syscallExec = 2,
			syscallJoin = 3, syscallCreate = 4, syscallOpen = 5,
			syscallRead = 6, syscallWrite = 7, syscallClose = 8,
			syscallUnlink = 9;

	/**
	 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
	 * <i>syscall</i> argument identifies which syscall the user executed:
	 * 
	 * <table>
	 * <tr>
	 * <td>syscall#</td>
	 * <td>syscall prototype</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td><tt>void halt();</tt></td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td><tt>void exit(int status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td><tt>int  join(int pid, int *status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td><tt>int  creat(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>5</td>
	 * <td><tt>int  open(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>6</td>
	 * <td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>7</td>
	 * <td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>8</td>
	 * <td><tt>int  close(int fd);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>9</td>
	 * <td><tt>int  unlink(char *name);</tt></td>
	 * </tr>
	 * </table>
	 * 
	 * @param syscall
	 *            the syscall number.
	 * @param a0
	 *            the first syscall argument.
	 * @param a1
	 *            the second syscall argument.
	 * @param a2
	 *            the third syscall argument.
	 * @param a3
	 *            the fourth syscall argument.
	 * @return the value to be returned to the user.
	 */
	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
		case syscallHalt:
			if (pid == 1) // is root process 
				return handleHalt();
			else 
				return -1;
			
		case syscallExit:
			this.exitCause = 1;
			this.exitValue = a0;
			handleExit();
			break;
			
		case syscallExec:
			try {
				UserProcess child = UserProcess.newUserProcess();
				String filename = this.readVirtualMemoryString(a0, maxFilenameLength);
				String[] args = new String[a1];
				byte[] argv = new byte[a1 * 4];
				this.readVirtualMemory(a2, argv);
				for (int i = 0; i < a1; i++) {
					args[i] = this.readVirtualMemoryString(Lib.bytesToInt(argv, i*4, 4), maxStringArgumentLength);
				}
				
				if (!child.execute(filename, args))
					return -1;
				children.add(child);
				return child.pid;
			} catch (Exception e) {
				Lib.debug('S', e.toString());
				return -1;
			}
			
		case syscallJoin:
			try {
				UserProcess childToJoin = null;
				for(int i = 0; i < children.size(); i++) {
					if (children.get(i).pid == a0) {
						childToJoin = children.get(i);
						break;
					}
				}
				if (childToJoin == null) 
					return -1;
				children.remove(childToJoin);
				if (childToJoin.exitCause == -2){
					childToJoin.thread.join();
				}

				childToJoin.thread.join();
				this.writeVirtualMemory(a1, Lib.bytesFromInt(childToJoin.exitValue));
				return childToJoin.exitCause;
			} catch (Exception e){
				Lib.debug('S', e.toString());
				return -1;
			}
		case syscallCreate:
			try {
				String filename = this.readVirtualMemoryString(a0, maxFilenameLength);
				if (filename == null) 
					return -1; 

				OpenFile file = UserKernel.fileSystem.open(filename, true);
				if (file == null)
					return -1;
				
				int fileDescriptor = this.getNewFileDescriptor();
				if (fileDescriptor == -1)
					return -1; 
				
				fileTable[fileDescriptor] = file;
				return fileDescriptor;
				
			} catch (Exception e) {
				Lib.debug('S', e.toString());
				return -1;
			}
			
		case syscallOpen:
			try {
				String filename = this.readVirtualMemoryString(a0, maxFilenameLength);
				if (filename == null) 
					return -1; 

				OpenFile file = UserKernel.fileSystem.open(filename, false);
				if (file == null)
					return -1;
				
				int fileDescriptor = this.getNewFileDescriptor();
				if (fileDescriptor == -1)
					return -1; 
				
				fileTable[fileDescriptor] = file;
				return fileDescriptor;
				
			} catch (Exception e) {
				Lib.debug('S', e.toString());
				return -1;
			}
						
		case syscallRead:
			try {
				OpenFile file = fileTable[a0];
				if (file == null)
					return -1;
				
				byte[] buf = new byte[a2+1];
				int count = file.read(buf, 0, a2);
				if (count == -1)
					return -1;
				
				return this.writeVirtualMemory(a1, buf, 0, count);
					
			} catch (Exception e) {
				Lib.debug('S', e.toString());
				return -1;
			}
		case syscallWrite:
			try {
				OpenFile file = fileTable[a0];
				if (file == null)
					return -1;
				
				byte[] buf = new byte[a2+1];
				int count = this.readVirtualMemory(a1, buf, 0, a2);
				if (count == -1)
					return -1;
				
				file.write(buf, 0, count);
				return count;
					
			} catch (Exception e) {
				Lib.debug('S', e.toString());
				return -1;
			}
		
		case syscallClose:
			try {
				OpenFile file = fileTable[a0];
				if (file == null)
					return -1;
				
				file.close();
				fileTable[a0] = null;
				return 0;
			} catch (Exception e) {
				Lib.debug('S', e.toString());
				return -1;
			}
			
		case syscallUnlink:
			try {
				String filename = this.readVirtualMemoryString(a0, maxFilenameLength);
				if (UserKernel.fileSystem.remove(filename))
					return 0;
				return -1;
			} catch (Exception e) {
				Lib.debug('S', e.toString());
				return -1;
			}
			
		default:
			this.exitCause = 0;
			this.exitValue = -1;
			handleExit();			
//			Lib.debug(dbgProcess, "Unknown syscall " + syscall);
//			Lib.assertNotReached("Unknown system call!");
		}
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
		case Processor.exceptionSyscall:
			int result = handleSyscall(processor.readRegister(Processor.regV0),
					processor.readRegister(Processor.regA0), processor
							.readRegister(Processor.regA1), processor
							.readRegister(Processor.regA2), processor
							.readRegister(Processor.regA3));
			processor.writeRegister(Processor.regV0, result);
			processor.advancePC();
			break;

			
		default:
			this.exitCause = 0;
			this.exitValue = -1;
			handleExit();
			
//			Lib.debug(dbgProcess, "Unexpected exception: "
//					+ Processor.exceptionNames[cause]);
//			Lib.assertNotReached("Unexpected exception");
		}
	}

	
	private int getNewFileDescriptor() {
		for (int i = 0; i < maxFileOpened; i++) {
			if (fileTable[i] == null) {
				return i;
			}
		}
		return -1;
	}
	
	public int getPid() {
		return pid;
	}
	/** The program being run by this process. */
	protected Coff coff;

	/** This process's page table. */
	private TranslationEntry[] pageTable;
	/** The number of contiguous pages occupied by the program. */
	protected int numPages;

	/** The number of pages in the program's stack. */
	protected final int stackPages = Config.getInteger("Processor.numStackPages", 8);
	
	public static final int maxFilenameLength = 256;
	private static final int maxStringArgumentLength = 256;
	protected final int maxFileOpened = 32;
	/** File descriptor table*/
	protected OpenFile[] fileTable = new OpenFile[maxFileOpened];

	private int initialPC, initialSP;
	private int argc, argv;
	protected int pid = numCreated++;

	private ArrayList<UserProcess> children = new ArrayList<UserProcess>();
	UThread thread;
	private int exitValue;
	private int exitCause = -2;
	
	
	private static int numCreated = 1;
	private static int numRunning = 0;


	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';
	
	
}
