package fr.unice.miage.sd.tinydfs.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

import fr.unice.miage.sd.tinydfs.nodes.Slave;

public class SlaveImpl extends UnicastRemoteObject implements Slave {

	private String masterHost;
	private String dfsRootFolder;
	private int slaveId;
	private File slaveDir;

	private Slave slaveLeft = null;
	private Slave slaveRight = null;

	public SlaveImpl(String masterHost, String dfsRootFolder, int slaveId) throws RemoteException {
		super();
		this.masterHost = masterHost;
		this.dfsRootFolder = dfsRootFolder;
		this.slaveId = slaveId;

		// Créer le répertoire propre au Slave
		//
		try {
			slaveDir = new File(dfsRootFolder, String.valueOf(slaveId));
			if(!slaveDir.exists()) {
				Files.createDirectory(slaveDir.toPath());
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					Thread.sleep(2500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				try {

					try {
						slaveLeft = (Slave) Naming.lookup("rmi://" +SlaveImpl.this.masterHost + "/" + "slave"+(2*SlaveImpl.this.slaveId +2));
					} catch(NotBoundException e) {
						//e.printStackTrace();
						slaveLeft = null;
					}

					try {
						slaveRight = (Slave) Naming.lookup("rmi://" +SlaveImpl.this.masterHost + "/" + "slave"+(2*SlaveImpl.this.slaveId + 3));
					} catch(NotBoundException e) {
						//e.printStackTrace();
						slaveRight = null;
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	@Override
	public int getId() throws RemoteException {
		return slaveId;
	}

	@Override
	public Slave getLeftSlave() throws RemoteException {
		return slaveLeft;
	}

	@Override
	public Slave getRightSlave() throws RemoteException {
		return slaveRight;
	}

	@Override
	public void setLeftSlave(Slave slave) throws RemoteException {
		slaveLeft = slave;

	}

	@Override
	public void setRightSlave(Slave slave) throws RemoteException {
		slaveRight = slave;

	}

	@Override
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException {

		int index = getIndexOfFirstContentNotNull(subFileContent);
		if(index != -1) {

			saveByteArray(filename, subFileContent.get(index));
			Logger.getInstance().log("SlaveImpl " + getId() + " saveByteArray");
			subFileContent.set(index, null);

			if(!isListNull(subFileContent)) {

				List<byte[]> leftList = new LinkedList<byte[]>();
				List<byte[]> rightList = new LinkedList<byte[]>();

				splitContentList(subFileContent, leftList, rightList);

				if(getLeftSlave() != null && getRightSlave() != null) {
					getLeftSlave().subSave(filename, leftList);
					getRightSlave().subSave(filename, rightList);
				}

			}
		}

	}

	private void splitContentList(List<byte[]> subFileContent, List<byte[]> leftList, List<byte[]> rightList) {

		for(int i = 0; i < subFileContent.size(); i++) {

			if(i % 2 == 0) {
				leftList.add(subFileContent.get(i));
			} else {
				rightList.add(subFileContent.get(i));
			}
		}

	}

	private int getIndexOfFirstContentNotNull(List<byte[]> subFileContent) {

		for(int i = 0; i < subFileContent.size(); i++) {
			if(subFileContent.get(i) != null) return i;
		}

		return -1;
	}

	private boolean isListNull(List<byte[]> subFileContent) {

		for(byte[] content : subFileContent) {
			if(content != null) return false;
		}

		return true;
	}

	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {

		List<byte[]> result = new LinkedList<byte[]>();

		result.add(getByteArray(filename));
		if(getLeftSlave() != null && getRightSlave() != null) {
			result.addAll(getLeftSlave().subRetrieve(filename));
			result.addAll(getRightSlave().subRetrieve(filename));
		}

		return result.contains(null) ? null : result;
	}

	private byte[] getByteArray(String filename) {

		File f = new File(slaveDir, filename);

		if(f.exists()) {
			Logger.getInstance().log("SlaveImpl getByArray File exists");
			try {
				return Files.readAllBytes(f.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		try {
			Logger.getInstance().log("SlaveImpl " + getId() + " getByArray File exists");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return null;

	}

	private void saveByteArray(String filename, byte[] content) {

		File f = new File(slaveDir, filename);

		if(f.exists()) f.delete();

		try {
			Files.write(f.toPath(), content, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

}
