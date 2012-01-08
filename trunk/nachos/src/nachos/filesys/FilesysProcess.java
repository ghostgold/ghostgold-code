package nachos.filesys;

import nachos.machine.Machine;
import nachos.machine.Processor;
import nachos.vm.VMProcess;

/**
 * FilesysProcess is used to handle syscall and exception through some callback
 * methods.
 * 
 * @author starforever
 */
public class FilesysProcess extends VMProcess {
	protected static final int SYSCALL_MKDIR = 14;
	protected static final int SYSCALL_RMDIR = 15;
	protected static final int SYSCALL_CHDIR = 16;
	protected static final int SYSCALL_GETCWD = 17;
	protected static final int SYSCALL_READDIR = 18;
	protected static final int SYSCALL_STAT = 19;
	protected static final int SYSCALL_LINK = 20;
	protected static final int SYSCALL_SYMLINK = 21;

	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
		case SYSCALL_MKDIR:
			try {
				if (FilesysKernel.realFileSystem.createFolder(readVirtualMemoryString(a0, maxFilenameLength)))
					return 0;
			} catch(Exception e) {
				return -1;
			}
			return -1;
		case SYSCALL_RMDIR: 
			try {
				if (FilesysKernel.realFileSystem.removeFolder(readVirtualMemoryString(a0, maxFilenameLength)))
					return 0;
			} catch(Exception e) {
				return -1;
			}
			return -1;
		case SYSCALL_CHDIR: 
			try {
				if (FilesysKernel.realFileSystem.changeCurFolder(readVirtualMemoryString(a0, maxFilenameLength))) 
					return 0;
			} catch(Exception e) {
				return -1;
			}
			return -1;
		case SYSCALL_GETCWD: {
		}

		case SYSCALL_READDIR: {
		}

		case SYSCALL_STAT: {
		}

		case SYSCALL_LINK: {
		}

		case SYSCALL_SYMLINK: {
		}

		default:
			return super.handleSyscall(syscall, a0, a1, a2, a3);
		}
	}

	public void handleException(int cause) {
		if (cause == Processor.exceptionSyscall) {
			int result = handleSyscall(processor.readRegister(Processor.regV0),
					processor.readRegister(Processor.regA0), processor
							.readRegister(Processor.regA1), processor
							.readRegister(Processor.regA2), processor
							.readRegister(Processor.regA3));
			processor.writeRegister(Processor.regV0, result);
			processor.advancePC();
		} else
			super.handleException(cause);
	}
	Processor processor = Machine.processor();
}