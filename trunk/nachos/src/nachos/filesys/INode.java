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
//	public int getSector(int pos) {
//		if (pos >= file_size)
//			return 0;
//		if (pos < INode.DIRECT_NUM * Disk.SectorSize)
//			return direct[pos / Disk.SectorSize];
//		pos -= INode.DIRECT_NUM * Disk.SectorSize;
//		if (pos < (Disk.SectorSize / 4) * Disk.SectorSize) {
//			int[] singleDirect = loadPointers(singleIndirect);
//			return singleDirect[pos / Disk.SectorSize];
//		}
//		pos -= (Disk.SectorSize / 4) * Disk.SectorSize;
//		int[] doubleDirect = loadPointers(doubleIndirect);
//		for (int i = 0; i < Disk.SectorSize / 4; i++) { 
//			if (pos < (Disk.SectorSize / 4) * Disk.SectorSize) {
//				int[] singleDirect = loadPointers(doubleDirect[i]);
//				return singleDirect[pos / Disk.SectorSize];
//			}
//			pos -= (Disk.SectorSize / 4) * Disk.SectorSize;
//		}
//		return 0;
//	}

	public int read(int pos, byte[] buffer, int start, int limit) {
		if (pos + limit > file_size)
			limit = file_size - pos;
//		int firstSector = pos / Disk.SectorSize;
//		int lastSector = (pos + limit - 1) / Disk.SectorSize;
//		byte[] tmp = new byte[(lastSector - firstSector + 1) * Disk.SectorSize];
//		for (int i = firstSector; i <= lastSector; i++ ) {
//			int sectorNum = getSector(i * Disk.SectorSize );
//			if (sectorNum < 2)
//				System.out.println("Wrong sector number");
//			FilesysKernel.disk.readSector(sectorNum, tmp, (i - firstSector) * Disk.SectorSize);
//		}
//		System.arraycopy(tmp, pos - firstSector * Disk.SectorSize, buffer, start, limit);
//		return limit;
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
//		int firstSector = pos / Disk.SectorSize;
//		int lastSector = (pos + limit - 1) / Disk.SectorSize;
//		byte[] result = new byte[(lastSector - firstSector + 1) * Disk.SectorSize];
//		System.arraycopy(buffer, start, result, pos - firstSector * Disk.SectorSize, limit);
//		
//		byte[] tmp = new byte[Disk.SectorSize];
//		
//		int sectorNum = getSector(firstSector * Disk.SectorSize);
//		FilesysKernel.disk.readSector(sectorNum, tmp, 0);
//		System.arraycopy(tmp, 0, result, 0, pos - firstSector * Disk.SectorSize);
//		
//		sectorNum = getSector(lastSector * Disk.SectorSize);
//		FilesysKernel.disk.readSector(sectorNum, tmp, 0);
//		int copyFrom = pos + limit - lastSector * Disk.SectorSize;
//		if (copyFrom < Disk.SectorSize)
//			System.arraycopy(tmp, copyFrom , result, pos + limit - firstSector * Disk.SectorSize, Disk.SectorSize - copyFrom);
//
//		for (int i = firstSector; i <= lastSector; i ++) {
//			sectorNum = getSector(i * Disk.SectorSize);
//			if (sectorNum < 2)
//				System.out.println("Wrong sector number");
//			FilesysKernel.disk.writeSector(sectorNum, result, (i - firstSector) * Disk.SectorSize);
//		}
//		return limit;
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
	
	void allocSingleIndirect(int address, int from, int amount) {
		int[] singleDirect;
		if (from > 0) {
			singleDirect = loadPointers(singleIndirect);
		}
		else {
			singleDirect = new int[Disk.SectorSize / 4];
		}
		for (int i = from; i < from + amount; i++) {
			singleDirect[i] = FilesysKernel.realFileSystem.getFreeList().allocate();
		}
		savePointers(address, singleDirect);
	}
	
	void allocDoubleIndirect(int address, int from, int amount) {
		int[] doubleDirect;
		if (from > 0) {
			doubleDirect = loadPointers(address);
		}
		else {
			doubleDirect = new int[Disk.SectorSize / 4];
		}
		
		int firstSingleIndirect = from / (Disk.SectorSize / 4);
		
		for (int i = firstSingleIndirect; i < Disk.SectorSize / 4; i++) {
			int singleFrom = from - from / (Disk.SectorSize / 4);
			int singleAmount = min(amount, Disk.SectorSize / 4 - singleFrom);
			if (singleFrom == 0) {
				doubleDirect[i] = FilesysKernel.realFileSystem.getFreeList().allocate();
			}
			allocSingleIndirect(doubleDirect[i], singleFrom, singleAmount);
			amount -= singleAmount;
			from += singleAmount;
			if (amount == 0)
				break;
		}
		savePointers(address, doubleDirect);
	}
	
	/** change the file size and adjust the content in the inode accordingly */
	public void setFileSize(int size) {
		if (size > (INode.DIRECT_NUM + Disk.SectorSize / 4 + (Disk.SectorSize / 4) * (Disk.SectorSize / 4)) * Disk.SectorSize) {
			System.out.println("File too large");
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
				for (int i = oldSectors; i < INode.DIRECT_NUM && i < newSectors; i++) {
					direct[i] = FilesysKernel.realFileSystem.getFreeList().allocate();
					alloc--;
					oldSectors++;
					if (alloc == 0)
						break;
				}
			}
			if (alloc > 0 && oldSectors >= INode.DIRECT_NUM && oldSectors < INode.DIRECT_NUM + Disk.SectorSize / 4) {
				if (singleIndirect == 0) {
					singleIndirect = FilesysKernel.realFileSystem.getFreeList().allocate();
				}
				int amount = min(alloc, Disk.SectorSize / 4 + INode.DIRECT_NUM - oldSectors);
				allocSingleIndirect(singleIndirect, oldSectors - INode.DIRECT_NUM, amount);
				alloc -= amount;
				oldSectors += amount;
			}
			if (alloc > 0 && oldSectors > INode.DIRECT_NUM + Disk.SectorSize / 4) {
				if (doubleIndirect == 0) {
					doubleIndirect = FilesysKernel.realFileSystem.getFreeList().allocate();
				}
				allocDoubleIndirect(doubleIndirect, oldSectors - INode.DIRECT_NUM - Disk.SectorSize / 4, alloc);
			}
		}
		else {
			if (oldSectors > INode.DIRECT_NUM + Disk.SectorSize / 4) {
				int from = max(0, newSectors - INode.DIRECT_NUM + Disk.SectorSize / 4);
				freeDoubleIndirect(doubleIndirect, from, oldSectors - INode.DIRECT_NUM + Disk.SectorSize / 4);
				if (from == 0) {
					FilesysKernel.realFileSystem.getFreeList().deallocate(doubleIndirect);
					doubleIndirect = 0;
				}
			}
			
			if (oldSectors > INode.DIRECT_NUM) {
				int from = max(0, newSectors - INode.DIRECT_NUM);
				freeSingleIndirect(singleIndirect, from, min(oldSectors - INode.DIRECT_NUM, Disk.SectorSize / 4));
				if (from == 0) {
					FilesysKernel.realFileSystem.getFreeList().deallocate(singleIndirect);
					singleIndirect = 0;
				}
			}
			
			if (newSectors < INode.DIRECT_NUM) {
				for (int i = newSectors; i < min(INode.DIRECT_NUM, oldSectors); i++) {
					FilesysKernel.realFileSystem.getFreeList().deallocate(direct[i]);
					direct[i] = 0;
				}
			}
		}
	}
	
	void freeDoubleIndirect(int address, int from, int to) {
		int[] doubleDirect = loadPointers(address); 
		int firstSingleIndirect = from / (Disk.SectorSize / 4);
		int lastSingleIndirect = (to - 1) / (Disk.SectorSize / 4);
		
		for(int i = firstSingleIndirect; i <= lastSingleIndirect; i++) {
			int singleFrom = (i * (Disk.SectorSize / 4) < from)? (from - (i * (Disk.SectorSize / 4))) : 0; 
			int singleTo = ((i + 1) * (Disk.SectorSize / 4) <= to)? (Disk.SectorSize / 4) : (to - (i * (Disk.SectorSize / 4))); 
			freeSingleIndirect(doubleDirect[i], singleFrom, singleTo);
			if (from == 0) {
				FilesysKernel.realFileSystem.getFreeList().deallocate(doubleDirect[i]);
				doubleDirect[i] = 0;
			}
		}
		savePointers(address, doubleDirect);
	}
	
	void freeSingleIndirect(int address, int from, int to) {
		int[] singleDirect = loadPointers(address);
		for (int i = from; i < to; i ++) {
			FilesysKernel.realFileSystem.getFreeList().deallocate(singleDirect[i]);
		}
		if (from > 0) {
			savePointers(address, singleDirect);
		}
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