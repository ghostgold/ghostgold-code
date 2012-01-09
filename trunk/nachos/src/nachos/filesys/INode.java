package nachos.filesys;

import java.util.LinkedList;

import nachos.machine.Disk;
import nachos.machine.Lib;

/**
 * INode contains detail information about a file. Most important among these is
 * the list of sector numbers the file occupied, it's necessary to find all the
 * pieces of the file in the filesystem.
 * 
 * @author starforever
 */
public class INode {
	/** represent a system file (free list) */
	public static int TYPE_SYSTEM = 0;

	/** represent a folder */
	public static int TYPE_FOLDER = 1;

	/** represent a normal file */
	public static int TYPE_FILE = 2;

	/** represent a normal file that is marked as delete */
	public static int TYPE_FILE_DEL = 3;

	/** represent a symbolic link file */
	public static int TYPE_SYMLINK = 4;

	/** represent a folder that are not valid */
	public static int TYPE_FOLDER_DEL = 5;

	/** the reserve size (in byte) in the first sector */
	private static final int FIRST_SEC_RESERVE = 16;

	
	public static int DIRECT_NUM = 122;
	/** size of the file in bytes */
	int file_size;

	/** the type of the file */
	int file_type;

	/** the number of programs that have access on the file */
	//int use_count;

	/** the number of links on the file */
	int link_count;

	/** maintain all the sector numbers this file used in order */
	private int[] direct;
	private int singleIndirect;
	private int doubleIndirect;

	/** the first address */
	int addr;
	

	public INode(int addr) {
		file_size = 0;
		//use_count = 0;
		link_count = 1;
		this.addr = addr;
		direct = new int[DIRECT_NUM];
		singleIndirect = 0;
		doubleIndirect = 0;
	}

	/** get the sector number of a position in the file */


	public int read(int pos, byte[] buffer, int start, int limit) {
		if (pos + limit > file_size)
			limit = file_size - pos;
		int countDown = limit;
		if (pos < Disk.SectorSize * INode.DIRECT_NUM && countDown > 0) {
			int firstSector = pos / Disk.SectorSize;
			for(int i = firstSector; i < INode.DIRECT_NUM; i++) {
				int directamount = readDirect(direct[i], pos % Disk.SectorSize, countDown, buffer, start);
				pos += directamount;
				countDown -= directamount;
				start += directamount;
				if (countDown == 0)
					break;
			}
		}
		pos -= Disk.SectorSize * INode.DIRECT_NUM;
		if (pos < Disk.SectorSize * (Disk.SectorSize / 4) && countDown > 0) {
			int amount = readSingleIndirect(singleIndirect, pos, countDown, buffer, start);
			pos += amount;
			countDown -= amount;
			start += amount;
		}
		pos -= Disk.SectorSize * (Disk.SectorSize / 4);
		if (countDown > 0) {
			int amount = readDoubleIndirect(doubleIndirect, pos, countDown, buffer, start);
			pos += amount;
			countDown -= amount;
			start += amount;
		}
		return limit - countDown;
	}
	int readDoubleIndirect(int secNum, int pos, int amount, byte[] data, int offset) {
		if (pos + amount > Disk.DiskSize * (Disk.SectorSize / 4) * (Disk.SectorSize / 4)) {
			amount = Disk.DiskSize * (Disk.SectorSize / 4) * (Disk.SectorSize / 4) - pos;
		}
		int[] doubleDirect = loadPointers(secNum);
		int countDown = amount;
		int firstSector = pos / (Disk.SectorSize * Disk.SectorSize / 4);
		for (int i = firstSector; i < doubleDirect.length; i++) {
			if (doubleDirect[i] < 2) {
				Lib.debug('f', "Wrong Sector Num");
			}
			int directamount = readSingleIndirect(doubleDirect[i], pos % (Disk.SectorSize * Disk.SectorSize / 4), countDown, data, offset);
			pos += directamount;
			countDown -= directamount;
			offset += directamount;
			if (countDown == 0) {
				break;
			}
		}
		return amount - countDown;
	}

	int readSingleIndirect(int secNum, int pos, int amount, byte[] data, int offset) {
		if (pos + amount > Disk.DiskSize * Disk.SectorSize / 4) {
			amount = Disk.SectorSize * Disk.SectorSize / 4 - pos;
		}
		int[] singleDirect = loadPointers(secNum);
		int firstSector = pos / Disk.SectorSize;
		int countDown = amount;
		for (int i = firstSector; i < singleDirect.length; i++) {
			if (singleDirect[i] < 2) {
				Lib.debug('f', "Wrong Sector Number");
			}
			int directamount = readDirect(singleDirect[i], pos % Disk.SectorSize, countDown, data, offset);
			pos += directamount;
			countDown -= directamount;
			offset += directamount;
			if (countDown == 0)
				break;
		}
		return amount - countDown;		
	}
	public int readDirect(int secNum, int pos, int amount, byte[] data, int offset) {
		if (pos + amount > Disk.SectorSize) {
			amount = Disk.SectorSize - pos;
		}
		byte[] tmp = new byte[Disk.SectorSize];
		FilesysKernel.disk.readSector(secNum, tmp, 0);
		System.arraycopy(tmp, pos, data, offset, amount);
		return amount;
	}

	public int write(int pos, byte[] buffer, int start, int limit) {
		if (pos + limit > file_size) {
			setFileSize(pos + limit);
			limit = min(limit, file_size - pos);
		}
		int countDown = limit;
		if (pos < Disk.SectorSize * INode.DIRECT_NUM && countDown > 0) {
			int firstSector = pos / Disk.SectorSize;
			for (int i = firstSector; i < INode.DIRECT_NUM; i++) {
				int amount = writeDirect(direct[i], pos % Disk.SectorSize, countDown, buffer, start);
				pos += amount;
				countDown -= amount;
				start += amount;
				if (countDown == 0)
					break;
			}
		}
		pos -= Disk.SectorSize * INode.DIRECT_NUM;
		if (pos < Disk.SectorSize * (Disk.SectorSize / 4) && countDown > 0) {
			int amount = writeSingleIndirect(singleIndirect, pos, countDown, buffer, start);
			pos += amount;
			countDown -= amount;
			start += amount;
		}
		pos -= Disk.SectorSize * (Disk.SectorSize / 4);
		if (countDown > 0) {
			int amount = writeDoubleIndirect(doubleIndirect, pos, countDown, buffer, start);
			pos += amount;
			countDown -= amount;
			start += amount;
		}
		return limit - countDown;
	}
	
	int writeDoubleIndirect(int secNum, int pos, int amount, byte[] data, int offset) {
		if (pos + amount > Disk.DiskSize * (Disk.SectorSize / 4) * (Disk.SectorSize / 4)) {
			amount = Disk.DiskSize * (Disk.SectorSize / 4) * (Disk.SectorSize / 4) - pos;
		}
		int[] doubleDirect = loadPointers(secNum);
		int countDown = amount;
		int firstSector = pos / (Disk.SectorSize * Disk.SectorSize / 4);
		for (int i = firstSector; i < doubleDirect.length; i++) {
			if (doubleDirect[i] < 2) {
				Lib.debug('f', "Wrong Sector Num");
			}
			int directamount = writeSingleIndirect(doubleDirect[i], pos % (Disk.SectorSize * Disk.SectorSize / 4), countDown, data, offset);
			pos += directamount;
			countDown -= directamount;
			offset += directamount;
			if (countDown == 0) {
				break;
			}
		}
		return amount - countDown;
	}
	
	int writeSingleIndirect(int secNum, int pos, int amount, byte[] data, int offset) {
		if (pos + amount > Disk.DiskSize * Disk.SectorSize / 4) {
			amount = Disk.SectorSize * Disk.SectorSize / 4 - pos;
		}
		int[] singleDirect = loadPointers(secNum);
		int firstSector = pos / Disk.SectorSize;
		int countDown = amount;
		for (int i = firstSector; i < singleDirect.length; i++) {
			if (singleDirect[i] < 2) {
				Lib.debug('f', "Wrong Sector Number");
			}
			int directamount = writeDirect(singleDirect[i], pos % Disk.SectorSize, countDown, data, offset);
			pos += directamount;
			countDown -= directamount;
			offset += directamount;
			if (countDown == 0)
				break;
		}
		return amount - countDown;
	}
	
	int writeDirect(int secNum, int pos, int amount, byte[] data, int offset) {
		if (pos + amount > Disk.SectorSize) {
			amount = Disk.SectorSize - pos;
		}
		byte[] tmp = new byte[Disk.SectorSize];
		if (amount != Disk.SectorSize) {
			FilesysKernel.disk.readSector(secNum, tmp, 0);
		}
		System.arraycopy(data, offset, tmp, pos, amount);
		FilesysKernel.disk.writeSector(secNum, tmp, 0);
		return amount;
	}
	
	int[] loadPointers(int address) {
		int[] result = new int[Disk.SectorSize / 4];
		byte[] tmp = new byte[Disk.SectorSize];
		FilesysKernel.disk.readSector(address, tmp, 0);
		for (int i = 0; i < result.length; i++) {
			result[i] = Disk.intInt(tmp, i * 4);
		}
		return result;
	}
	
	void savePointers(int address, int[] pointers) {
		byte[] result = new byte[Disk.SectorSize];
		for (int i = 0; i < pointers.length; i++) {
			Disk.extInt(pointers[i], result, i * 4);
		}
		FilesysKernel.disk.writeSector(address, result, 0);
	}
	
	int allocSingleIndirect(int address, int pos, int amount) {
		int[] singleDirect;
		if (pos + amount > Disk.SectorSize / 4) {
			amount = Disk.SectorSize / 4 - pos;
		}
		if (pos > 0) {
			singleDirect = loadPointers(singleIndirect);
		}
		else {
			singleDirect = new int[Disk.SectorSize / 4];
		}
		for (int i = pos; i < pos + amount; i++) {
			singleDirect[i] = FilesysKernel.realFileSystem.getFreeList().allocate();
		}
		savePointers(address, singleDirect);
		return amount;
	}
	
	int allocDoubleIndirect(int address, int pos, int amount) {
		int[] doubleDirect;
		if (pos > 0) {
			doubleDirect = loadPointers(address);
		}
		else {
			doubleDirect = new int[Disk.SectorSize / 4];
		}
		
		int firstSingleIndirect = pos / (Disk.SectorSize / 4);
		int countDown = amount;
		for (int i = firstSingleIndirect; i < doubleDirect.length; i++) {
			if (pos % (Disk.SectorSize / 4) == 0) {
				doubleDirect[i] = FilesysKernel.realFileSystem.getFreeList().allocate();
			}
			int alloc = allocSingleIndirect(doubleDirect[i], pos % (Disk.SectorSize / 4), countDown);
			countDown -= alloc;
			pos += alloc;
			if (countDown == 0)
				break;
		}
		savePointers(address, doubleDirect);
		return amount - countDown; 
	}
	
	/** change the file size and adjust the content in the inode accordingly */
	public void setFileSize(int size) {
		if (size > (INode.DIRECT_NUM + Disk.SectorSize / 4 + (Disk.SectorSize / 4) * (Disk.SectorSize / 4)) * Disk.SectorSize) {
			Lib.debug('f',"File too large");
			size = (INode.DIRECT_NUM + Disk.SectorSize / 4 + (Disk.SectorSize / 4) * (Disk.SectorSize / 4)) * Disk.SectorSize;
		}
		int oldSize = file_size;
		file_size = size;
		int oldSectors = (oldSize + Disk.SectorSize - 1) / Disk.SectorSize;
		int newSectors = (file_size + Disk.SectorSize - 1) / Disk.SectorSize;
		if (oldSectors == newSectors)
			return;
		if (oldSectors < newSectors) {
			int alloc = newSectors - oldSectors;
			if (alloc > 0 && oldSectors < INode.DIRECT_NUM) {
				for (int i = oldSectors; i < INode.DIRECT_NUM; i++) {
					direct[i] = FilesysKernel.realFileSystem.getFreeList().allocate();
					alloc--;
					oldSectors++;
					if (alloc == 0)
						break;
				}
			}
			oldSectors -= INode.DIRECT_NUM;
			if (alloc > 0 &&  oldSectors < Disk.SectorSize / 4) {
				if (oldSectors == 0) {
					singleIndirect = FilesysKernel.realFileSystem.getFreeList().allocate();
				}
				int amount = allocSingleIndirect(singleIndirect, oldSectors, alloc);
				alloc -= amount;
				oldSectors += amount;
			}
			oldSectors -= Disk.SectorSize / 4;
			if (alloc > 0 && oldSectors < (Disk.SectorSize / 4) * (Disk.SectorSize / 4)) {
				if (oldSectors == 0) {
					doubleIndirect = FilesysKernel.realFileSystem.getFreeList().allocate();
				}
				allocDoubleIndirect(doubleIndirect, oldSectors, alloc);
			}
		}
		else {
			int amount = oldSectors - newSectors;
			if (newSectors < INode.DIRECT_NUM) {
				for (int i = newSectors; i < INode.DIRECT_NUM; i++) {
					FilesysKernel.realFileSystem.getFreeList().deallocate(direct[i]);
					amount--;
					newSectors++;
					if (amount == 0)
						break;
				}
			}
			newSectors -= INode.DIRECT_NUM;
			if (newSectors < Disk.SectorSize / 4 && amount > 0) {
				int release =  freeSingleIndirect(singleIndirect, newSectors, amount);
				amount -= release;
				newSectors += release; 
			}
			newSectors -= Disk.SectorSize / 4;
			if (newSectors < (Disk.SectorSize / 4) * (Disk.SectorSize / 4) && amount > 0) {
				int release = freeDoubleIndirect(doubleIndirect, newSectors, amount);
				amount -= release;
				newSectors += release;
			}
		}
	}
	
	int freeDoubleIndirect(int address, int pos, int amount) {
		int[] doubleDirect = loadPointers(address); 
		int firstSingleIndirect = pos / (Disk.SectorSize / 4);
		int countDown = amount;
		for(int i = firstSingleIndirect; i < doubleDirect.length; i++) {
			int release = freeSingleIndirect(doubleDirect[i], pos % (Disk.SectorSize / 4), countDown);
			countDown -= release;
			pos += release;
			if (pos % (Disk.SectorSize / 4) == 0) {
				FilesysKernel.realFileSystem.getFreeList().deallocate(doubleDirect[i]);
				doubleDirect[i] = 0;
			}
			if (countDown == 0)
				break;
		}
		savePointers(address, doubleDirect);
		return amount - countDown;
	}
	
	int freeSingleIndirect(int address, int pos, int amount) {
		int[] singleDirect = loadPointers(address);
		if (pos + amount > Disk.SectorSize / 4) {
			amount = Disk.SectorSize - pos;
		}
		for (int i = pos; i < pos + amount; i ++) {
			FilesysKernel.realFileSystem.getFreeList().deallocate(singleDirect[i]);
		}
		if (pos > 0) {
			savePointers(address, singleDirect);
		}
		return amount;
	}
	
	int min(int a, int b) {
		if (a < b)
			return a;
		return b;
	}
	
	int max(int a, int b) {
		if (a > b)
			return a;
		return b;
	}

	/** free the disk space occupied by the file (including inode) */
	public void free() {
		this.setFileSize(0);
		FilesysKernel.realFileSystem.getFreeList().deallocate(addr);
	}

	/** load inode content from the disk */
	public void load() {
		byte[] tmp = new byte[Disk.SectorSize];
		FilesysKernel.disk.readSector(addr, tmp, 0);
		file_size = Disk.intInt(tmp, 0);
		file_type = Disk.intInt(tmp, 4);
		link_count = Disk.intInt(tmp, 8);
		
		for (int i = 0; i < INode.DIRECT_NUM; i ++) {
			direct[i] = Disk.intInt(tmp, 16 + i * 4);
		}
		singleIndirect = Disk.intInt(tmp, 16 + INode.DIRECT_NUM * 4);
		doubleIndirect = Disk.intInt(tmp, 20 + INode.DIRECT_NUM * 4);
	}

	/** save inode content to the disk */
	public void save() {
		byte[] tmp = new byte[Disk.SectorSize];
		Disk.extInt(file_size, tmp, 0);
		Disk.extInt(file_type, tmp, 4);
		Disk.extInt(link_count, tmp, 8);
		
		for (int i = 0; i < INode.DIRECT_NUM; i ++) {
			Disk.extInt(direct[i], tmp, 16 + i * 4);
		}
		Disk.extInt(singleIndirect, tmp,  16 + INode.DIRECT_NUM * 4);
		Disk.extInt(doubleIndirect, tmp,  20 + INode.DIRECT_NUM * 4);
		FilesysKernel.disk.writeSector(addr, tmp, 0);
	}
	
}
