package nachos.filesys;

import nachos.machine.Lib;
import nachos.machine.Machine;
import nachos.machine.Processor;
import nachos.userprog.UserProcess;
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
		case SYSCALL_GETCWD: 
			try {
				byte[] buffer = new byte[a1 + 1];
				int pos = 0;
				String current = FilesysKernel.realFileSystem.cur_folder;
				while(pos < current.length() && pos < a1) {
					buffer[pos] = (byte)current.charAt(pos);
					pos++;
				}
				buffer[pos++] = 0;
				writeVirtualMemory(a0, buffer, 0, pos);
				return pos;
			} catch(Exception e) {
				return -1;
			}
		case SYSCALL_READDIR: {
		}

		case SYSCALL_STAT:
			try {
				FileStat stat = FilesysKernel.realFileSystem.getStat(readVirtualMemoryString(a0, maxFilenameLength));
				if (stat == null)
					return -1;
				byte[] result = new byte[UserProcess.maxFilenameLength + 20];
				int pos = 0;
				while (pos < stat.name.length()) {
					result[pos] = (byte)stat.name.charAt(pos);
					pos++;
				}
				result[pos++] = 0;
				pos = 256;
				Lib.bytesFromInt(result, pos, stat.size); 
				pos += 4;
				Lib.bytesFromInt(result, pos, stat.sectors); 
				pos += 4;
				Lib.bytesFromInt(result, pos, stat.type); 
				pos += 4;
				Lib.bytesFromInt(result, pos, stat.inode); 
				pos += 4;
				Lib.bytesFromInt(result, pos, stat.links); 
				pos += 4;
				writeVirtualMemory(a1, result, 0, pos);
				return 0;
			} catch(Exception e) {
				return -1;
			}
		case SYSCALL_LINK: 
			try {
				if (FilesysKernel.realFileSystem.createLink(readVirtualMemoryString(a0, maxFilenameLength), readVirtualMemoryString(a1, maxFilenameLength)))
					return 0;
			} catch(Exception e) {
				return -1;
			}
			return -1;
		case SYSCALL_SYMLINK: 
			try {
				if (FilesysKernel.realFileSystem.createSymlink(readVirtualMemoryString(a0, maxFilenameLength), readVirtualMemoryString(a1, maxFilenameLength)))
					return 0;
			} catch(Exception e) {
				return -1;
			}
			return -1;


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
