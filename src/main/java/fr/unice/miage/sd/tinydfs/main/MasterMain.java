package fr.unice.miage.sd.tinydfs.main;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.nodes.MasterImpl;

public class MasterMain {

	// Usage: java fr.unice.miage.sd.tinydfs.main.MasterMain
	// storage_service_name dfs_root_folder nb_slaves
	public static void main(String[] args) {
		String storageServiceName = args[0];
		String dfsRootFolder = args[1] + "/Master/";
		int nbSlaves = Integer.parseInt(args[2]);

		// Verify folder exist
		File folder = new File(dfsRootFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		// Create master and register it
		try {
			Master master = new MasterImpl(nbSlaves, dfsRootFolder);
			Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			registry.bind(storageServiceName, master);
			System.out.println("Server ready : " + storageServiceName);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}

	}

}
