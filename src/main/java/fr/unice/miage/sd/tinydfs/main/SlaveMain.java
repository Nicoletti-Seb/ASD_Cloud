package fr.unice.miage.sd.tinydfs.main;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import fr.unice.miage.sd.tinydfs.impl.Logger;
import fr.unice.miage.sd.tinydfs.impl.SlaveImpl;
import fr.unice.miage.sd.tinydfs.nodes.Slave;

public class SlaveMain { 
	
	// Usage: java fr.unice.miage.sd.tinydfs.main.SlaveMain master_host dfs_root_folder slave_identifier
	public static void main(String[] args) {
		String masterHost = args[0];
		String dfsRootFolder = args[1];
		int slaveId = Integer.parseInt(args[2]);
		
		// Create slave and register it (registration name must be "slave" + slave identifier)
		try {
			Slave slave = new SlaveImpl(masterHost, dfsRootFolder, slaveId);
			Naming.rebind("rmi://" + masterHost + "/" + "slave" +slaveId, slave);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}