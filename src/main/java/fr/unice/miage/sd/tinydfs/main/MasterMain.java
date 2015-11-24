package fr.unice.miage.sd.tinydfs.main;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fr.unice.miage.sd.tinydfs.impl.MasterImpl;
import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.tests.config.Constants;

public class MasterMain {

	// Usage: java fr.unice.miage.sd.tinydfs.main.MasterMain storage_service_name dfs_root_folder nb_slaves
	public static void main(String[] args) {
		String storageServiceName = args[0];
		String dfsRootFolder = args[1];
		int nbSlaves = Integer.parseInt(args[2]);
		
		// Create master and register it
		
		try {

			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			Master master = new MasterImpl(storageServiceName, dfsRootFolder, nbSlaves);
			Naming.rebind("rmi://localhost/" + Constants.SERVICE_NAME_PROPERTY_KEY, master);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
}
