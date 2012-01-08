package nachos.filesys;

import nachos.threads.Lock;

public class LockedFile {
	INode inode;
	int useCount = 0;
	Lock lock = new Lock();
	
	public LockedFile(INode inode) {
		this.inode = inode;
		useCount = 1;
	}
	public int read(int pos, byte[] buffer, int start, int limit) {
		lock.acquire();
		int num = inode.read(pos, buffer, start, limit);
		lock.release();
		return num;
	}

	public int write(int pos, byte[] buffer, int start, int limit) {
		lock.acquire();
		int num = inode.write(pos, buffer, start, limit);
		lock.release();
		return num;
	}
	
	public void setSize(int size) {
		lock.acquire();
		inode.setFileSize(size);
		lock.release();
	}
	public void close() {
		lock.acquire();
		useCount--;
		inode.save();		
		if (useCount == 0) {
			FilesysKernel.realFileSystem.openedINode.remove(new Integer(inode.addr));
			FilesysKernel.realFileSystem.openedFile.remove(new Integer(inode.addr));
			if (inode.link_count == 0)
				inode.free();
		}
		lock.release();
	}
}
