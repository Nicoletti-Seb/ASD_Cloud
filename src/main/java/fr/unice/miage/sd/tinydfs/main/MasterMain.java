package fr.unice.miage.sd.tinydfs.main;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;

import fr.unice.miage.sd.tinydfs.nodes.ServerCreator;

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

		// Create registry and server
		try {
			Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			
			Timer timer = new Timer();
			timer.schedule(new ServerCreator(registry, storageServiceName, dfsRootFolder, nbSlaves), 100, 100);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		

	}

}
