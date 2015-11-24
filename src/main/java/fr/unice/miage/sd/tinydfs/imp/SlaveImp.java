package fr.unice.miage.sd.tinydfs.imp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import fr.unice.miage.sd.tinydfs.nodes.Slave;

public class SlaveImp extends UnicastRemoteObject implements Slave{

	protected SlaveImp() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getId() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Slave getLeftSlave() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Slave getRightSlave() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLeftSlave(Slave slave) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRightSlave(Slave slave) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subSave(String filename, List<byte[]> subFileContent)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
