package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Master extends Remote {

	/**
	 * @return the dfsRootFolder
	 * @throws RemoteException
	 */
	public String getDfsRootFolder() throws RemoteException;

	/**
	 * @return the number of slaves
	 * @throws RemoteException
	 */
	public int getNbSlaves() throws RemoteException;

	/**
	 * Save a file.
	 * 
	 * @param file
	 * @throws RemoteException
	 */
	public void saveFile(File file) throws RemoteException;

	/**
	 * Save a byte array.
	 * 
	 * @param filename
	 * @param fileContent
	 * @throws RemoteException
	 */
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException;

	/**
	 * @param filename
	 * @return the file associate with filename, return null if not exist.
	 * @throws RemoteException
	 */
	public File retrieveFile(String filename) throws RemoteException;

	/**
	 * @param filename
	 * @return the byte array associate with filename, return null if not exist
	 * @throws RemoteException
	 */
	public byte[] retrieveBytes(String filename) throws RemoteException;

	// -------------------------------------- Methode add
	/**
	 * Allow to add a slave to a master. The master create links between the new
	 * slave and parent
	 * 
	 * @param slave
	 * @return true, if add
	 * @throws RemoteException
	 */
	public boolean addSlave(Slave slave) throws RemoteException;

	/**
	 * Get the file's size
	 * 
	 * @param filename
	 * @return
	 * @throws RemoteException
	 */
	public int getSizeFile(String filename) throws RemoteException;
}
