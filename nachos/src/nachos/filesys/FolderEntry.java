package nachos.filesys;

/**
 * FolderEntry contains information used by Folder to map from filename to
 * address of the file
 * 
 * @author starforever
 * */
class FolderEntry {
	public FolderEntry(String filename, int addr) {
		this.name = filename;
		this.addr = addr;
	}
	/** the file name */
	String name;

	/** the sector number of the inode */
	int addr;
}
