package fr.unice.miage.sd.tinydfs.nodes;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.TimerTask;

/**
 *  This class allow to create a server.
 *  
 *  - the class wait the connection slaves and create the tree.
 *
 */
public class ServerCreator extends TimerTask{
	
	private Registry registry;
	
	private Slave[] slaves;
	private int nbConnected;
	
	public ServerCreator(Registry registry, String serviceName, String dfsRootFolder, int nbSlaves) {
		this.registry = registry;
		this.slaves = new Slave[nbSlaves];
		
		createMaster(serviceName, dfsRootFolder, slaves);
	}

	@Override
	public void run() {
		
		try {
			Slave slave = (Slave) registry.lookup("rmi://localhost/Slave-" + nbConnected);
			addSlave(slave);
			
			if( slaves.length <= nbConnected ){
				cancel();
			}
			
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("rmi://localhost/Slave-"+ nbConnected + " n'as pas pu être trouvée...");
		}
		
	}
	
	/**
	 * Add the slave in the tree.
	 * @param slave
	 * @return true, if the slave is added.
	 * @throws RemoteException
	 */
	private boolean addSlave(Slave slave) throws RemoteException {
		slaves[nbConnected] = slave;

		// create the link with parents
		if (nbConnected > 1) {
			int indexParent = (nbConnected >> 1) - 1; // (i / 2) -1
			if ((nbConnected & 1) == 0) { // i is divisible by 2
				slaves[indexParent].setLeftSlave(slave);
			} else {
				slaves[indexParent].setRightSlave(slave);
			}
		}

		nbConnected++;
		
		return true;
	}
	
	/**
	 * Create the master 
	 * @param serviceName
	 * @param dfsRootFolder
	 * @param slaves
	 */
	private void createMaster(String serviceName, String dfsRootFolder, Slave[] slaves){
		try {
			Master master = new MasterImpl(slaves, dfsRootFolder);
			registry.bind(serviceName, master);
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
