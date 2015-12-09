package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

import fr.unice.miage.sd.tinydfs.main.files.ManagerFiles;

/**
 * The Class SlaveImpl.
 */
public class SlaveImpl extends UnicastRemoteObject implements Slave {

	private static final long serialVersionUID = 1L;

	private ManagerFiles managerFiles;
	private Slave slaveR;
	private Slave slaveL;
	private int id;

	/**
	 * Instantiates a new slave impl.
	 *
	 * @param id
	 *            the id
	 * @param dfsRootFolder
	 *            the dfs root folder
	 * @throws RemoteException
	 *             the remote exception
	 */
	public SlaveImpl(int id, String dfsRootFolder) throws RemoteException {
		super();
		this.id = id;
		managerFiles = new ManagerFiles(dfsRootFolder);
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Slave#subSave(java.lang.String,
	 * java.util.List)
	 */
	@Override
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException {
		// Get and remove the first element
		byte[] data = subFileContent.get(0);
		subFileContent.remove(data);

		// Save the element
		managerFiles.saveFile(filename, data);

		if (slaveR == null || slaveL == null) {
			return;
		}

		// cut the list & save
		int middle = subFileContent.size() / 2;

		// The subfile must be wrap because sublist
		// return an instance of 'RandomAccessSubList'
		// which is not serializable.
		slaveL.subSave(filename, new LinkedList<>(subFileContent.subList(0, middle)));
		slaveR.subSave(filename, new LinkedList<>(subFileContent.subList(middle, subFileContent.size())));
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Slave#subRetrieve(java.lang.String)
	 */
	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {
		File file = new File(managerFiles.getDfsRootFolder() + "/" + filename);
		if (!file.exists()) {
			return null;
		}

		List<byte[]> dataList = null;
		if (slaveL == null || slaveR == null) {
			dataList = new LinkedList<>();
		} else {
			dataList = slaveL.subRetrieve(filename);
			dataList.addAll(slaveR.subRetrieve(filename));
		}
		dataList.add(managerFiles.readFile(filename));

		return dataList;
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Slave#getSizeFile(java.lang.String)
	 */
	@Override
	public int getSizeFile(String filename) throws RemoteException {
		int size = managerFiles.getSizeFile(filename);

		if (slaveL != null && slaveR != null) {
			size += slaveL.getSizeFile(filename) + slaveR.getSizeFile(filename);
		}

		return size;
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Slave#getId()
	 */
	@Override
	public int getId() throws RemoteException {
		return id;
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Slave#getLeftSlave()
	 */
	@Override
	public Slave getLeftSlave() throws RemoteException {
		return slaveL;
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Slave#getRightSlave()
	 */
	@Override
	public Slave getRightSlave() throws RemoteException {
		return slaveR;
	}

	/*
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Slave#setLeftSlave(fr.unice.miage.sd.
	 * tinydfs.nodes.Slave)
	 */
	@Override
	public void setLeftSlave(Slave slave) throws RemoteException {
		slaveL = slave;
	}

	/*
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Slave#setRightSlave(fr.unice.miage.sd.
	 * tinydfs.nodes.Slave)
	 */
	@Override
	public void setRightSlave(Slave slave) throws RemoteException {
		slaveR = slave;
	}
}
