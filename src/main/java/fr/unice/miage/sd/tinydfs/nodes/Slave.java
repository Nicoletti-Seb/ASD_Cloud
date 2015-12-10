package fr.unice.miage.sd.tinydfs.nodes;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The Interface Slave.
 */
public interface Slave extends Remote {

	/**
	 * @return the slave's id
	 * @throws RemoteException
	 */
	public int getId() throws RemoteException;
	
	/**
	 * @return the left slave, return null if not exist
	 * @throws RemoteException
	 */
	public Slave getLeftSlave() throws RemoteException;
	
	/**
	 * @return @return the right slave, return null if not exist
	 * @throws RemoteException
	 */
	public Slave getRightSlave()  throws RemoteException;

	/**
	 * Set the left slave.
	 * @param slave
	 * @throws RemoteException
	 */
	public void setLeftSlave(Slave slave) throws RemoteException;

	/**
	 * Set the right slave
	 * @param slave
	 * @throws RemoteException
	 */
	public void setRightSlave(Slave slave) throws RemoteException;
	
	/**
	 * Save the byte array with the name filename.
	 * @param filename
	 * @param subFileContent
	 * @throws RemoteException
	 */
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException;

	/**
	 * @param filename
	 * @return the byte array list associate with filename.
	 * @throws RemoteException
	 */
	public List<byte[]> subRetrieve(String filename) throws RemoteException;
	
	/**
	 * @param filename
	 * @return the size of file associate with filename.
	 * @throws RemoteException
	 */
	public int getSizeFile(String filename) throws RemoteException;

}
