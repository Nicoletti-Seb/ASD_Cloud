package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

import fr.unice.miage.sd.tinydfs.main.files.ManagerFiles;

/**
 * The Class MasterImpl.
 * 
 * @author Sébatien Nicoletti
 */
public class MasterImpl extends UnicastRemoteObject implements Master {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Slave[] slaves;

	private ManagerFiles managerFiles;

	/**
	 * Instantiates a new master impl.
	 *
	 * @throws RemoteException
	 *             the remote exception
	 */
	public MasterImpl(int nbSlaves, String dfsRootFolder) throws RemoteException {
		super();
		this.slaves = new Slave[nbSlaves];
		managerFiles = new ManagerFiles(dfsRootFolder);
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#getDfsRootFolder()
	 */
	@Override
	public String getDfsRootFolder() throws RemoteException {
		return managerFiles.getDfsRootFolder();
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#getNbSlaves()
	 */
	@Override
	public int getNbSlaves() throws RemoteException {
		return slaves.length;
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#saveFile(java.io.File)
	 * 
	 */
	@Override
	public void saveFile(File file) throws RemoteException {
		List<byte[]> partFile = managerFiles.cutFile(file, getNbSlaves());

		int middleIndex = getNbSlaves() >> 1; // div by 2;
		
		slaves[0].subSave(file.getName(), new LinkedList<>(partFile.subList(0, middleIndex)) );
		slaves[1].subSave(file.getName(), new LinkedList<>(partFile.subList(middleIndex, partFile.size())) );
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#saveBytes(java.lang.String,
	 * byte[])
	 */
	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		List<byte[]> partFile = managerFiles.cutByteArray(fileContent, getNbSlaves());

		int middleIndex = getNbSlaves() >> 1; // div by 2;

		slaves[0].subSave(filename, new LinkedList<>(partFile.subList(0, middleIndex)));
		slaves[1].subSave(filename, new LinkedList<>(partFile.subList(middleIndex, partFile.size())));
	}

	/*
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Master#retrieveFile(java.lang.String)
	 */
	@Override
	public File retrieveFile(String filename) throws RemoteException {
		List<byte[]> subFileContentLeft = slaves[0].subRetrieve(filename);
		List<byte[]> subFileContentRight = slaves[1].subRetrieve(filename);

		if (subFileContentLeft.isEmpty() || subFileContentRight.isEmpty()) {
			return null;
		}

		// Save block file
		saveListBlockFile(filename, subFileContentLeft);
		saveListBlockFile(filename, subFileContentRight);

		// Build and return the file
		return managerFiles.buildFile(filename, getNbSlaves());
	}

	/**
	 * Save data blocks
	 * 
	 * @param filename:
	 *            the file's name build with this blocks
	 * @param list:
	 *            the block list
	 */
	private void saveListBlockFile(String filename, List<byte[]> list) {
		for (byte[] block : list) {
			managerFiles.saveFile(filename + "-" + block[0], block);
		}
	}

	/*
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Master#retrieveBytes(java.lang.String)
	 */
	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		List<byte[]> subFileContentLeft = slaves[0].subRetrieve(filename);
		List<byte[]> subFileContentRight = slaves[1].subRetrieve(filename);

		if (subFileContentLeft.isEmpty() || subFileContentRight.isEmpty()) {
			return null;
		}

		// Save block file
		saveListBlockFile(filename, subFileContentLeft);
		saveListBlockFile(filename, subFileContentRight);

		// Build and return the byte array
		return managerFiles.buildByteArray(filename, getNbSlaves());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Master#addSlave(fr.unice.miage.sd.tinydfs
	 * .nodes.Slave)
	 */
	@Override
	public boolean addSlave(Slave slave) throws RemoteException {
		int i = 0;
		for (; i < getNbSlaves(); i++) {
			if (slaves[i] == null) {
				slaves[i] = slave;
				break;
			}
		}

		if (i == getNbSlaves()) {
			return false;
		}

		// create the link with the parent
		if (i > 1) {
			int indexParent = (i >> 1) - 1; // (i / 2) -1
			if ((i & 1) == 0) { // i is divisible by 2
				slaves[indexParent].setLeftSlave(slave);
			} else {
				slaves[indexParent].setRightSlave(slave);
			}
		}

		return true;
	}

	@Override
	public int getSizeFile(String filename) throws RemoteException {
		if (slaves[0] == null || slaves[1] == null) {
			return -1;
		}

		// Reduce the first byte to build the file.
		return slaves[0].getSizeFile(filename) + slaves[1].getSizeFile(filename) - getNbSlaves();
	}
}
