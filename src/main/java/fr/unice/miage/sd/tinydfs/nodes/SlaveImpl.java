package fr.unice.miage.sd.tinydfs.nodes;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class SlaveImpl extends UnicastRemoteObject implements Slave {

	private static final long serialVersionUID = 1L;

	private Slave slaveR;
	private Slave slaveL;
	private int id;

	protected SlaveImpl(int id) throws RemoteException {
		super();
		this.id = id;
	}

	@Override
	public int getId() throws RemoteException {
		return id;
	}

	@Override
	public Slave getLeftSlave() throws RemoteException {
		return slaveL;
	}

	@Override
	public Slave getRightSlave() throws RemoteException {
		return slaveR;
	}

	@Override
	public void setLeftSlave(Slave slave) throws RemoteException {
		slaveL = slave;
	}

	@Override
	public void setRightSlave(Slave slave) throws RemoteException {
		slaveR = slave;
	}

	@Override
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
