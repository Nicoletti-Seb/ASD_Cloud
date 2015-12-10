package fr.unice.miage.sd.tinydfs.nodes;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BufferInterface extends Remote {
	public Integer ajoutFini() throws RemoteException;
	public void slaveAjouter() throws RemoteException;
}
