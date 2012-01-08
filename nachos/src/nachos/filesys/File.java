package nachos.filesys;

import nachos.machine.OpenFile;

/**
 * File provide some basic IO operations. Each File is associated with an INode
 * which stores the basic information for the file.
 * 
 * @author starforever
 */
public class File extends OpenFile {
	LockedFile file;

	private int pos;

	public File(LockedFile file) {
		this.file = file;
		file.useCount++;
		pos = 0;
	}

	public int length() {
		return file.inode.file_size;
	}

	public void close() {
		file.close();
	}

	public void seek(int pos) {
		this.pos = pos;
		if (pos > file.inode.file_size) {
			file.setSize(pos);
		}
	}

	public int tell() {
		return pos;
	}

	public int read(byte[] buffer, int start, int limit) {
		int ret = read(pos, buffer, start, limit);
		pos += ret;
		return ret;
	}

	public int write(byte[] buffer, int start, int limit) {
		int ret = write(pos, buffer, start, limit);
		pos += ret;
		return ret;
	}

	public int read(int pos, byte[] buffer, int start, int limit) {
		return file.read(pos, buffer, start, limit);
	}

	public int write(int pos, byte[] buffer, int start, int limit) {
		return file.write(pos, buffer, start, limit);
	}
}
