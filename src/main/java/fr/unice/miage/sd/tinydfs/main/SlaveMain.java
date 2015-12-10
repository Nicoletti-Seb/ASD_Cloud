package fr.unice.miage.sd.tinydfs.main;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fr.unice.miage.sd.tinydfs.imp.MasterImp;
import fr.unice.miage.sd.tinydfs.imp.SlaveImp;
import fr.unice.miage.sd.tinydfs.nodes.BufferInterface;
import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.nodes.Slave;


public class SlaveMain { 
	
	// Usage: java fr.unice.miage.sd.tinydfs.main.SlaveMain master_host dfs_root_folder slave_identifier
	public static void main(String[] args) {
		String masterHost = args[0];
		String dfsRootFolder = args[1];
		int slaveId = Integer.parseInt(args[2]);
		
		
		// Create slave and register it (registration name must be "slave" + slave identifier)
		try {
			Slave sI;
			sI = new SlaveImp(masterHost,dfsRootFolder,slaveId);
			
			//instance enregistré dans le registre
			Naming.rebind("rmi://" + masterHost + "/slave_"+sI.getId(), sI);
			BufferInterface bI = (BufferInterface)Naming.lookup("rmi://"+masterHost+"/Controleur");
			bI.slaveAjouter();
		} catch (RemoteException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}