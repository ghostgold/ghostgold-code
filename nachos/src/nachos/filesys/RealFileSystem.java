package nachos.filesys;

import java.util.HashMap;
import java.util.LinkedList;

import nachos.machine.Disk;
import nachos.machine.FileSystem;
import nachos.machine.Machine;
import nachos.machine.OpenFile;

/**
 * RealFileSystem provide necessary methods for filesystem syscall. The
 * FileSystem interface already define two basic methods, you should implement
 * your own to adapt to your task.
 * 
 * @author starforever
 */
public class RealFileSystem implements FileSystem {
	private static final int MAX_LINK_DEPTH = 10;

	/** the free list */
	private FreeList free_list;

	/** the root folder */
	//private Folder root_folder;

	/** the current folder */
	//private Folder cur_folder;

	int cur_folder_address = Folder.STATIC_ADDR;
	String cur_folder = "/";
	/** the string representation of the current folder */
	//private LinkedList<String> cur_path = new LinkedList<String>();
	
	HashMap<Integer, INode> openedINode = new HashMap<Integer, INode>();
	HashMap<Integer, LockedFile> openedFile = new HashMap<Integer, LockedFile>();
	HashMap<Integer, Folder> openedFolder = new HashMap<Integer, Folder>();
	
	/**
	 * initialize the file system
	 * 
	 * @param format
	 *            whether to format the file system
	 */
	public void init(boolean format) {
		if (format) {
			INode inode_free_list = new INode(FreeList.STATIC_ADDR);
			inode_free_list.file_type = INode.TYPE_SYSTEM;
			free_list = new FreeList(inode_free_list);
			free_list.init();
			free_list.save();
			
			INode inode_root_folder = new INode(Folder.STATIC_ADDR);
			inode_root_folder.file_type = INode.TYPE_FOLDER;
			Folder root_folder = new Folder(inode_root_folder);
			root_folder.setParent(Folder.STATIC_ADDR);
			root_folder.close();
 //			FilesysKernel.realFileSystem.openedFolder.put(new Integer(Folder.STATIC_ADDR), root_folder);
//			FilesysKernel.realFileSystem.openedINode.put(new Integer(Folder.STATIC_ADDR), inode_root_folder); 
			importStub();
		} else {
			INode inode_free_list = new INode(FreeList.STATIC_ADDR);
			inode_free_list.load();
			free_list = new FreeList(inode_free_list);
			free_list.load();

//			INode inode_root_folder = new INode(Folder.STATIC_ADDR);
//			inode_root_folder.load();
//			Folder root_folder = new Folder(inode_root_folder);
//			root_folder.load();
//			FilesysKernel.realFileSystem.openedFolder.put(new Integer(Folder.STATIC_ADDR), root_folder);
//			FilesysKernel.realFileSystem.openedINode.put(new Integer(Folder.STATIC_ADDR), inode_root_folder); 
		}
	}

	public void finish() {
		//root_folder.save();
		free_list.save();
	}

	/** import from stub filesystem */
	private void importStub() {
		FileSystem stubFS = Machine.stubFileSystem();
		FileSystem realFS = FilesysKernel.realFileSystem;
		String[] file_list = Machine.stubFileList();
		for (int i = 0; i < file_list.length; ++i) {
			if (!file_list[i].endsWith(".coff"))
				continue;
			OpenFile src = stubFS.open(file_list[i], false);
			if (src == null) {
				continue;
			}
			OpenFile dst = realFS.open(file_list[i], true);
			int size = src.length();
			byte[] buffer = new byte[size];
			src.read(0, buffer, 0, size);
			dst.write(0, buffer, 0, size);
			src.close();
			dst.close();
		}
	}

	/** get the only free list of the file system */
	public FreeList getFreeList() {
		return free_list;
	}

	/** get the only root folder of the file system */
	public Folder getRootFolder() {
		return getFolder(Folder.STATIC_ADDR);
	}

	public OpenFile open(String name, boolean create) {
		OpenFile file = nameiFile(name);
		if (file != null) 
			return file;
		else if (create) {
			Folder folder = nameiFolder(getDirectory(name));
			String filename = getFilename(name); 
			if (folder == null || folder.inode.link_count == 0) {
				return null;
			}
			int address = folder.create(filename);
			folder.close();
			return getFile(address);
		}
		return null;
	}

	OpenFile nameiFile(String name) {
		return getFile(namei(name));
	}
	
	Folder nameiFolder(String name) {
		return getFolder(namei(name));
	}
	
	int namei(String name) {
		String[] path = name.split("/");
		Folder start;
		if (name.startsWith("/"))
			start = getFolder(Folder.STATIC_ADDR);
		else {
			start = getFolder(cur_folder_address);

		}
		for (int i = 0; i < path.length - 1; i ++) {
			if (path[i].equals(".") || path[i].equals(""))
				continue;
			int newaddr = start.getEntry(path[i]);
			start.close();
			if (newaddr > 0) {
				start = getFolder(newaddr);
				if (start == null) 
					return 0;
			}
			else return 0;
		}
		if (path.length == 0 || path[path.length - 1].equals("") || path[path.length - 1].equals(".")) {
			int addr = start.inode.addr;
			start.close();
			return addr;
		}
		else {
			int addr = start.getEntry(path[path.length - 1]);
			start.close();
			return addr;
		}
	}
	
	String getFilename(String path) {
		String[] result = path.split("/");
		return result[result.length - 1];
	}
	String getDirectory(String path) {
		String[] result = path.split("/");
		StringBuffer directory = new StringBuffer();
		if (path.startsWith("/"))
			directory.append("/");
		for (int i = 0; i < result.length - 1; i ++) {
			directory.append(result[i] + "/");
		}
		return directory.toString();
	}
	

	INode getINode(int addr) {
		if (openedINode.get(new Integer(addr)) != null)
			return openedINode.get(new Integer(addr));
		INode inode = new INode(addr);
		inode.load();
		openedINode.put(new Integer(addr), inode); 
		return inode;
	}
	Folder getFolder(int addr) {
		Integer address = new Integer(addr);
		if (openedFolder.get(address) != null) {
			Folder folder = openedFolder.get(address);
			folder.useCount++;
			return folder;
		}
		
		else {
			INode inode = openedINode.get(address);
			if (inode == null) {
				inode = new INode(addr);
				inode.load();
				openedINode.put(address, inode);
			}
			if (inode.file_type == INode.TYPE_FOLDER){
				Folder folder = new Folder(inode);
				folder.load();
				openedFolder.put(address, folder);
				return folder;
			}
			else if (inode.file_type == INode.TYPE_SYMLINK) {
				openedINode.remove(address);
				return getFolder(nameiSymLink(addr, MAX_LINK_DEPTH));
			}
			return null;
		}
	}
	
	int nameiSymLink(int addr, int depth) {
		if (depth == 0)
			return 0;
		INode inode = getINode(addr);
		int result = 0;
		if (inode.file_type != INode.TYPE_SYMLINK) {
			result = inode.addr;
		}
		else {
			StringBuffer link = new StringBuffer();
			byte[] buffer = new byte[inode.file_size];
			inode.read(0, buffer, 0, buffer.length);
			for (int i = 0; i < buffer.length; i ++) 
				link.append((char)buffer[i]);
			result = nameiSymLink(namei(link.toString()), depth - 1);
		}
		openedINode.remove(new Integer(addr));
		return result;
	}
	
	File getFile(int addr) {
		Integer address = new Integer(addr);
		if (openedFile.get(address) != null) 
			return new File(openedFile.get(address));
		else {
			INode inode = openedINode.get(address);
			if (inode == null) {
				inode = new INode(addr);
				inode.load();
				openedINode.put(address, inode);
			}
			if (inode.file_type == INode.TYPE_FILE){
				openedFile.put(address, new LockedFile(openedINode.get(address)));
				return new File(openedFile.get(address));
			}
			else if (inode.file_type == INode.TYPE_SYMLINK) {
				openedINode.remove(address);
				return getFile(nameiSymLink(inode.addr, MAX_LINK_DEPTH));
			}
			return null;
		}
	}
	
	public boolean remove(String name) {
		Folder parent = nameiFolder(getDirectory(name));
		if (parent == null)
			return false;
		int fileAddress = parent.getEntry(getFilename(name));
		if (fileAddress == 0)
			return false;
		INode inode = getINode(fileAddress);
		if (inode.file_type == INode.TYPE_SYMLINK) {
			inode.link_count--;
			if (inode.link_count == 0)
				inode.free();
			else 
				inode.save();
			openedINode.remove(new Integer(fileAddress));
			parent.removeEntry(getFilename(name));
			parent.close();
			return true;
		}
		File file = getFile(parent.getEntry(getFilename(name)));
		if (file != null) {
			parent.removeEntry(getFilename(name));
			file.file.inode.link_count--;
			file.close();
			parent.close();
			return true;
		}
		return false;
	}

	public boolean createFolder(String name) {
		String filename = getFilename(name);
		if (filename.equals("") || filename.equals(".") || filename.equals(".."))
			return false;
		Folder parent = nameiFolder(getDirectory(name));
		if (parent != null && parent.getEntry(filename) == 0 && parent.inode.link_count > 0) {
			INode inode = new INode(free_list.allocate());
			inode.file_type = INode.TYPE_FOLDER;
			Folder folder = new Folder(inode);
			openedINode.put(new Integer(inode.addr), inode);
			openedFolder.put(new Integer(inode.addr), folder);
			folder.setParent(parent.inode.addr);
			parent.addEntry(filename, inode.addr);
			folder.close();
			parent.close();
			return true;
		}
		return false;
	}
	public boolean createFolde(String name) {
		createFolder(name);
		return false;
	}

	public boolean removeFolder(String name) {
		String filename = getFilename(name);
		if (filename.equals("") || filename.equals(".") || filename.equals(".."))
			return false;
		Folder parent = nameiFolder(getDirectory(name));
		if (parent == null) {
			return false;
		}
		int folderAddress = parent.getEntry(filename);
		if (folderAddress == 0)
			return false;
		INode inode = getINode(folderAddress);
		if (inode.file_type == INode.TYPE_SYMLINK) {
			inode.link_count--;
			if (inode.link_count == 0)
				inode.free();
			else 
				inode.save();
			openedINode.remove(new Integer(folderAddress));
			parent.removeEntry(getFilename(name));
			parent.close();
			return true;
		}	
		Folder folder = getFolder(parent.getEntry(filename));
		if (parent != null && folder != null) {
			if (folder.inode.addr > 1 && folder.entry.size() == 1) {
				parent.removeEntry(getFilename(name));
				folder.inode.link_count--;
				folder.close();
				parent.close();
				return true;
			}
			else {
				folder.close();
				parent.close();
				return false;
			}
		}
		return false;
	}

	String makePath(String pre, String sub) {
		String[] pres = pre.split("/");
		String[] subs = sub.split("/");
		String[] result = new String[pres.length + subs.length];
		System.arraycopy(pres, 0, result, 0, pres.length);
		int next = pres.length;
		for(int i = 0; i < subs.length; i ++) {
			if (subs[i].equals(".") || subs[i].equals(""))
				continue;
			if (subs[i].equals("..")) {
				next--;
				if (next < 0)
					next = 0;
			}
			else 
				result[next++] = subs[i];
		}
		StringBuffer path = new StringBuffer();
		for (int i =0 ; i < next; i++) {
			if (result[i].equals(".") || result[i].equals(""))
				continue;
			path.append("/" + result[i]);
		}
		if (path.length() == 0) 
			path.append("/");
		return path.toString();
	}
	public boolean changeCurFolder(String name) {
		int newaddr = namei(name);
		if (newaddr > 0) {
			if (name.startsWith("/"))
				cur_folder = name;
			else 
				cur_folder = makePath(cur_folder, name);
			Folder folder = getFolder(newaddr);
			if(folder != null) {
				cur_folder_address = newaddr; 
				return true;
			}
			else
				return false;
		}
		return false;
	}

	public String[] readDir(String name) {
		// TODO implement this
		return null;
	}

	int calcSectors (int size) {
		int sectors = (size + Disk.SectorSize - 1) / Disk.SectorSize;
		if (sectors <= INode.DIRECT_NUM) 
			return sectors + 1; 
		if (size <= INode.DIRECT_NUM + Disk.SectorSize / 4)
			return sectors + 2;
		return sectors + (sectors - INode.DIRECT_NUM - Disk.SectorSize / 4 + Disk.SectorSize / 4 - 1) / (Disk.SectorSize / 4) + 3;
	}
	public FileStat getStat(String name) {
		File file = (File)nameiFile(name);
		if (file == null)
			return null;
		else {
			FileStat result = new FileStat();
			result.inode = file.file.inode.addr;
			result.links = file.file.inode.link_count;
			result.name = getFilename(name);
			result.sectors = calcSectors(file.file.inode.file_size);
			result.size = file.file.inode.file_size;
			result.type = file.file.inode.file_type;
			file.close();
			return result;
		}
	}

	public boolean createLink(String src, String dst) {
		String filename = getFilename(dst);
		if (filename.equals("") || filename.equals(".") || filename.equals(".."))
			return false;
		int addr = namei(src);
		if (addr == 0)
			return false;
		Folder parent = nameiFolder(getDirectory(dst));
		if (parent == null || parent.getEntry(filename) != 0)
			return false;
		parent.addEntry(filename, addr);
		parent.close();
		File file = getFile(addr);
		file.file.inode.link_count++;
		file.close();
		return true;
	}

	public boolean createSymlink(String src, String dst) {
		Folder folder = nameiFolder(getDirectory(dst));
		if (folder.getEntry(getFilename(dst)) != 0) {
			folder.close();
			return false;
		}
		
		if (namei(src) == 0) {
			return false;
		}
		
		INode inode = new INode(FilesysKernel.realFileSystem.getFreeList().allocate());
		inode.file_type = INode.TYPE_SYMLINK;
		String result;
		if (src.startsWith("/"))
			result = src;
		else 
			result = makePath(cur_folder, src);
		byte[] buffer = new byte[result.length()];
		for (int i = 0; i < buffer.length; i++)
			buffer[i] = (byte)result.charAt(i);
		inode.write(0, buffer, 0, buffer.length);
		inode.save();
		folder.addEntry(getFilename(dst), inode.addr);
		folder.save();
		return true;
	}

	public int getSwapFileSectors() {
		return 0;
	}

	public int getFreeSize() {
		return 0;
	}
	
}
