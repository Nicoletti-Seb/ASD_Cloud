package fr.unice.miage.sd.tinydfs.nodes;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Master extends Remote {
	
	public String getDfsRootFolder() throws RemoteException;
	
	public int getNbSlaves() throws RemoteException;
	
	public void saveFile(File file) throws RemoteException;
	
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException;
	
	public File retrieveFile(String filename) throws RemoteException;
	
	public byte[] retrieveBytes(String filename) throws RemoteException;
	
	// -------------------------------------- Methode add
	/**
	 * Allow to add a slave to a master.
	 * The master create links between the new slave and parent
	 * @param slave
	 * @return true, if add
	 * @throws RemoteException
	 */
	public boolean addSlave(Slave slave) throws RemoteException;

	/**
	 * Get the file's size
	 * @param filename
	 * @return
	 * @throws RemoteException
	 */
	public int getSizeFile(String filename) throws RemoteException;
}
