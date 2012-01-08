package nachos.filesys;

import nachos.machine.SynchDisk;

public class CacheSynchDisk {
	public CacheSynchDisk(SynchDisk disk) {
		this.disk = disk;
	}

	public void readSector(int sectorNumber, byte[] data, int index) {
		disk.readSector(sectorNumber, data, index);
	}

	public void writeSector(int sectorNumber, byte[] data, int index) {
		disk.writeSector(sectorNumber, data, index);
	}

	SynchDisk disk;
}
