package nachos.filesys;

import java.util.LinkedList;
import nachos.machine.Disk;
import nachos.machine.Lib;
import nachos.threads.Lock;

/**
 * FreeList is a single special file used to manage free space of the
 * filesystem. It maintains a list of sector numbers to indicate those that are
 * available to use. When there's a need to allocate a new sector in the
 * filesystem, call allocate(). And you should call deallocate() to free space
 * at a appropriate time (eg. when a file is deleted) for reuse in the future.
 * 
 * @author starforever
 */
public class FreeList {
	/** the static address */
	public static int STATIC_ADDR = 0;

	/** size occupied in the disk (bitmap) */
	static int size = Lib.divRoundUp(Disk.NumSectors, 8);

	/** maintain address of all the free sectors */
	private LinkedList<Integer> free_list;

	INode inode;
	
	Lock lock;
	public FreeList(INode inode) {
		this.inode = inode;
		free_list = new LinkedList<Integer>();
		lock = new Lock();
	}

	public void init() {
		for (int i = 2; i < Disk.NumSectors; ++i)
			free_list.add(i);
		inode.setFileSize(Disk.NumSectors / 8);
	}

	/** allocate a new sector in the disk */
	public int allocate() {
		//lock.acquire();
		if (free_list.size() > 0) {
			return free_list.removeFirst();
		}
		//lock.release();
		return 0;
	}

	/** deallocate a sector to be reused */
	public void deallocate(int sec) {
		//lock.acquire();
		free_list.add(new Integer(sec));
		//lock.release();
	}

	/** save the content of freelist to the disk */
	public void save() {
		byte[] result = new byte[Disk.NumSectors / 8];
		for (int i = 0; i < result.length; i ++) {
			result[i] = (byte) 0xFF;
		}
		for (Integer i: free_list) {
			result[i.intValue() / 8] = (byte) (result[i.intValue() / 8] ^ (1 << (i % 8))); 
		}
		inode.write(0, result, 0, result.length);
	}

	/** load the content of freelist from the disk */
	public void load() {
		byte[] result = new byte[Disk.NumSectors / (Disk.SectorSize * 8)];
		inode.read(0, result, 0, result.length);
		for (int i = 0; i < result.length; i ++) {
			for (int j = 0; j < 8; j++) {
				if (((result[i] >> j) & 1) == 0)
					free_list.add(i * 8 + j);
			}
		}
	}
}
