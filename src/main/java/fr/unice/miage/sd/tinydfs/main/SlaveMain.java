package fr.unice.miage.sd.tinydfs.main;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fr.unice.miage.sd.tinydfs.nodes.Slave;
import fr.unice.miage.sd.tinydfs.nodes.SlaveImpl;

public class SlaveMain {

	// Usage: java fr.unice.miage.sd.tinydfs.main.SlaveMain master_host
	// dfs_root_folder slave_identifier
	public static void main(String[] args) {
		String masterHost = args[0];
		int slaveId = Integer.parseInt(args[2]);
		String dfsRootFolder = args[1] + "/Slave/" + slaveId;

		// Verify folder exist
		File folder = new File(dfsRootFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		// Create slave and register it (registration name must be "slave" +
		// slave identifier)
		try {
			Slave slave = new SlaveImpl(slaveId, dfsRootFolder);
			Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
			registry.bind("rmi://" + masterHost + "/Slave-"+slaveId, slave);

			System.out.println("Slave connected : " + slaveId);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}

	}

}