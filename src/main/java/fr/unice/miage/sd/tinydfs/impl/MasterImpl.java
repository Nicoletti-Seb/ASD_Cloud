package fr.unice.miage.sd.tinydfs.impl;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import fr.unice.miage.sd.tinydfs.nodes.Master;


public class MasterImpl extends UnicastRemoteObject implements Master {

	private static final long serialVersionUID = -1855955039831240161L;
	
	private String storageServiceName;
	private String rootFolder;
	private int nbSlaves;
	
	public MasterImpl(String storageServiceName, String rootFoler, int nbSlaves) throws RemoteException {
		super();
		this.storageServiceName = storageServiceName;
		this.rootFolder = rootFoler;
		this.nbSlaves = nbSlaves;
	}
	
	@Override
	public String getDfsRootFolder() throws RemoteException {
		return rootFolder;
	}

	@Override
	public int getNbSlaves() throws RemoteException {
		return nbSlaves;
	}

	@Override
	public void saveFile(File file) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public File retrieveFile(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
