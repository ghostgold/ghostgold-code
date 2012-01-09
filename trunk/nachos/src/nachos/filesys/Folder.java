package nachos.filesys;

import java.util.Hashtable;

import nachos.machine.Disk;
import nachos.threads.Lock;

/**
 * Folder is a special type of file used to implement hierarchical filesystem.
 * It maintains a map from filename to the address of the file. There's a
 * special folder called root folder with pre-defined address. It's the origin
 * from where you traverse the entire filesystem.
 * 
 * @author starforever
 */
public class Folder  {
	/** the static address for root folder */
	public static int STATIC_ADDR = 1;

	private int size;
	int useCount = 0;
	INode inode;
	Lock lock;
	/** mapping from filename to folder entry */
	Hashtable<String, FolderEntry> entry;

	public Folder(INode inode) {
		size = 4;
		lock = new Lock();
		this.inode = inode;
		entry = new Hashtable<String, FolderEntry>();
		useCount = 1;
	}
	
	public void setParent(int parentAddr) {
		entry.put("..", new FolderEntry("..", parentAddr));
	}

	/** open a file in the folder and return its address */
	public int open(String filename) {
		return getEntry(filename);
	}

	/** create a new file in the folder and return its address */
	public int create(String filename) {
		if (getEntry(filename) != 0) {
			return getEntry(filename);
		}
		INode inode = new INode(FilesysKernel.realFileSystem.getFreeList().allocate());
		inode.file_type = INode.TYPE_FILE;
		FilesysKernel.realFileSystem.openedINode.put(new Integer(inode.addr), inode);
		addEntry(filename, inode.addr);
		return inode.addr;
	}

	/** add an entry with specific filename and address to the folder */
	public void addEntry(String filename, int addr) {
		lock.acquire();
		entry.put(filename, new FolderEntry(filename, addr));
		lock.release();
	}

	/** remove an entry from the folder */
	public void removeEntry(String filename) {
		lock.acquire();
		entry.remove(filename);
		lock.release();
	}

	public int getEntry(String filename) {
		lock.acquire();
		if (entry.get(filename) != null) {
			lock.release();
			return entry.get(filename).addr;
		}
		lock.release();
		return 0;
	}
	
	public void close() {
		save();
		useCount--;
		if (useCount == 0) {
			FilesysKernel.realFileSystem.openedFolder.remove(new Integer(inode.addr));
			FilesysKernel.realFileSystem.openedINode.remove(new Integer(inode.addr));
			if (inode.link_count == 0)
				inode.free();
		}
	}
	/** save the content of the folder to the disk */
	public void save() {
		lock.acquire();
		int length = 0; 
		for(String filename: entry.keySet()) {
			length += filename.length() + 5;
		}
		byte[] entrybyte = new byte[length];
		int pos = 0;
		for(FolderEntry file: entry.values()) {
			writeString(entrybyte, file.name, pos);
			pos += file.name.length() + 1;
			Disk.extInt(file.addr, entrybyte, pos);
			pos += 4;
		}
		inode.setFileSize(entrybyte.length);
		inode.write(0, entrybyte, 0, entrybyte.length);
		inode.save();
		lock.release();
	}

	void writeString(byte[] buffer, String s, int pos) {
		for (int i = 0; i < s.length(); i++) {
			buffer[pos + i] = (byte)s.charAt(i);
		}
		buffer[pos + s.length()] = 0;
	}
	/** load the content of the folder from the disk */
	public void load() {
		lock.acquire();
		byte[] entrybyte = new byte[inode.file_size];
		inode.read(0, entrybyte, 0, inode.file_size);
		entry.clear();
		parse(entrybyte);
		lock.release();
	}
	
	void parse(byte[] entrybyte) {
		int pos = 0;
		while (pos < entrybyte.length) {
			String filename = readString(entrybyte, pos);
			pos += filename.length() + 1;
			int entryaddress = Disk.intInt(entrybyte, pos);
			pos += 4;
			entry.put(filename, new FolderEntry(filename, entryaddress));
		}
	}
	
	String readString(byte[] buffer, int pos) {
		StringBuffer result = new StringBuffer();
		while (pos <  buffer.length) {
			if (buffer[pos] == 0)
				break;
			result.append((char)buffer[pos]);
			pos++;
		}
		return result.toString();
	}
}
