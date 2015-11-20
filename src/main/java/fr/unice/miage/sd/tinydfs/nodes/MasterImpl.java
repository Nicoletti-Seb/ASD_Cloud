package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * The Class MasterImpl.
 * 
 * @author Sébatien Nicoletti
 */
public class MasterImpl extends UnicastRemoteObject implements Master {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Slave[] slaves;

	private String dfsRootFolder;

	/**
	 * Instantiates a new master impl.
	 *
	 * @throws RemoteException
	 *             the remote exception
	 */
	protected MasterImpl(int nbSlaves, String dfsRootFolder) throws RemoteException {
		super();
		this.slaves = new Slave[nbSlaves];
		this.dfsRootFolder = dfsRootFolder;
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#getDfsRootFolder()
	 */
	@Override
	public String getDfsRootFolder() throws RemoteException {
		return dfsRootFolder;
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
		// TODO Auto-generated method stub
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#saveBytes(java.lang.String, byte[])
	 */
	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		// TODO Auto-generated method stub

	}

	/*
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Master#retrieveFile(java.lang.String)
	 */
	@Override
	public File retrieveFile(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Master#retrieveBytes(java.lang.String)
	 */
	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
