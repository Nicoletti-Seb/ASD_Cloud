package fr.unice.miage.sd.tinydfs.imp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import fr.unice.miage.sd.tinydfs.nodes.BufferInterface;

public class Controleur extends UnicastRemoteObject implements BufferInterface{
	private Integer nombre = 0 ;
	private Integer nombreAttendu = 0 ;
	public Controleur(int nA) throws RemoteException {
		super();
		nombreAttendu = nA;
	}

	
	public synchronized Integer ajoutFini() throws RemoteException{
		while(nombre!=nombreAttendu){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("erreur");
			}
		}
		return 1;
	}
	
	
	public synchronized void slaveAjouter () throws RemoteException{
		while (nombre == nombreAttendu){
			System.out.println("et ici");
			try {
				wait();

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("erreur");
			}
		}
		nombre +=1;		
		notifyAll();
	}

}
