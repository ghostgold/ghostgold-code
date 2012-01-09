package nachos.filesys;

import java.util.Arrays;

import nachos.machine.Disk;
import nachos.machine.SynchDisk;
import nachos.threads.Lock;

public class CacheSynchDisk {
	
	Lock lock;
	static final int CACHE_SIZE = Disk.NumSectors / 32 ;
	CacheEntry[] cache;
	public CacheSynchDisk(SynchDisk disk) {
		this.disk = disk;
		lock = new Lock();
		cache = new CacheEntry[CACHE_SIZE];
		for (int i = 0; i < CACHE_SIZE; i++) {
			cache[i] = new CacheEntry(0, false, false, new byte[Disk.SectorSize]);
		}
	}

	public void readSector(int sectorNumber, byte[] data, int index) {
		lock.acquire();
		int pos = sectorNumber % CACHE_SIZE;
		if (cache[pos].valid && cache[pos].sectorNumber != sectorNumber && cache[pos].dirty) {
			disk.writeSector(cache[pos].sectorNumber, cache[pos].data, 0);
		}

		if (!cache[pos].valid || cache[pos].sectorNumber != sectorNumber) {
			disk.readSector(sectorNumber, cache[pos].data, 0);
			cache[pos].dirty = false;
		}
		
		cache[pos].sectorNumber = sectorNumber;
		cache[pos].valid = true;
		//cache[pos].data = Arrays.copyOfRange(data, index, index + Disk.SectorSize);
		System.arraycopy(cache[pos].data, 0, data, index, Disk.SectorSize);

		lock.release();
	}

	public void writeSector(int sectorNumber, byte[] data, int index) {
		lock.acquire();
		int pos = sectorNumber % CACHE_SIZE;
		if (cache[pos].valid && cache[pos].sectorNumber != sectorNumber && cache[pos].dirty) {
			disk.writeSector(cache[pos].sectorNumber, cache[pos].data, 0);
		}
		System.arraycopy(data, index, cache[pos].data, 0, Disk.SectorSize);
		cache[pos].dirty = true;
		cache[pos].valid = true;
		//cache[pos].data = Arrays.copyOfRange(data, index, index + Disk.SectorSize);
		cache[pos].sectorNumber = sectorNumber;
		//disk.writeSector(sectorNumber, data, index);
		//disk.writeSector(sectorNumber, data, index);
		lock.release();
	}

	SynchDisk disk;
	
	class CacheEntry {
		byte[] data;
		int sectorNumber;
		boolean dirty;
		boolean valid;
		public CacheEntry(int sectorNumber, boolean valid, boolean dirty, byte[] data) {
			this.sectorNumber = sectorNumber;
			this.valid = valid;
			this.dirty = dirty;
			this.data = data;
		}
		public void setDirty() {
			this.dirty = true;
		}
	}
}
