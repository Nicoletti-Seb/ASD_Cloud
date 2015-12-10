package fr.unice.miage.sd.tinydfs.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fr.unice.miage.sd.tinydfs.imp.Controleur;
import fr.unice.miage.sd.tinydfs.imp.MasterImp;
import fr.unice.miage.sd.tinydfs.nodes.BufferInterface;
import fr.unice.miage.sd.tinydfs.nodes.Master;


public class MasterMain {

	// Usage: java fr.unice.miage.sd.tinydfs.main.MasterMain storage_service_name dfs_root_folder nb_slaves
	public static void main(String[] args) {
		String storageServiceName = args[0];
		String dfsRootFolder = args[1];
		int nbSlaves = Integer.parseInt(args[2]);
		BufferInterface ctrl=null;
		try {
			 ctrl = new Controleur(nbSlaves);
			
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Create master and register it
		try {
			//creation registre
			Registry r = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			Naming.rebind("rmi://localhost/Controleur" , ctrl);
			Master mI = new MasterImp(storageServiceName,dfsRootFolder,nbSlaves);
			//instance enregistré dans le registre
			Naming.rebind("rmi://localhost/" + storageServiceName, mI);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
