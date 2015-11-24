package fr.unice.miage.sd.tinydfs.main;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fr.unice.miage.sd.tinydfs.imp.MasterImp;
import fr.unice.miage.sd.tinydfs.nodes.Master;


public class MasterMain {

	// Usage: java fr.unice.miage.sd.tinydfs.main.MasterMain storage_service_name dfs_root_folder nb_slaves
	public static void main(String[] args) {
		String storageServiceName = args[0];
		String dfsRootFolder = args[1];
		int nbSlaves = Integer.parseInt(args[2]);
		
		// Create master and register it
		try {
			//creation registre
			Registry r = LocateRegistry.createRegistry(1099);
	
			Master mI = new MasterImp(storageServiceName,dfsRootFolder,nbSlaves);
			//instance enregistré dans le registre
			r.rebind("master", mI);
			mI.saveFile(new File("D:\\MIAGE\\M1\\systeme_distribue\\projet\\SHsjpJ.txt"));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
