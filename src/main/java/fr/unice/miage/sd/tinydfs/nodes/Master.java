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
	
	public float getSize(String fileName) throws RemoteException;
	
	public void setLeftSlave(Slave slave) throws RemoteException;

	public void setRightSlave(Slave slave) throws RemoteException;
	
	public void construire() throws RemoteException;
	
	
}
