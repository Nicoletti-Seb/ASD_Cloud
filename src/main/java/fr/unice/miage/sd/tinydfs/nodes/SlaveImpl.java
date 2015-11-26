package fr.unice.miage.sd.tinydfs.nodes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

public class SlaveImpl extends UnicastRemoteObject implements Slave {

	private static final long serialVersionUID = 1L;

	private String dfsRootFolder;
	private Slave slaveR;
	private Slave slaveL;
	private int id;

	protected SlaveImpl(int id, String dfsRootFolder) throws RemoteException {
		super();
		this.id = id;
		this.dfsRootFolder = dfsRootFolder;
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
		// Get and remove the first element
		byte[] data = subFileContent.get(0);
		subFileContent.remove(data);

		// Save the element
		File file = new File(dfsRootFolder + "/" + filename);
		savefile(file, data);
		if (slaveR == null || slaveL == null) {
			return;
		}

		// cut the list & save
		int middle = subFileContent.size() / 2;
		slaveL.subSave(filename, subFileContent.subList(0, middle));
		slaveR.subSave(filename, subFileContent.subList(middle, subFileContent.size()));

	}

	/**
	 * Allow to save the data in a file
	 * 
	 * @param file
	 * @param data
	 */
	private void savefile(File file, byte[] data) {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {
		File file = new File(dfsRootFolder + "/" + filename);
		if (!file.exists()){
			return null;
		}
		
		List<byte[]> dataList = null;
		if( slaveL == null || slaveR == null ){
			dataList = new LinkedList<>();
		}
		else{
			dataList = slaveL.subRetrieve(filename);
			dataList.addAll(slaveR.subRetrieve(filename));
		}
		dataList.add(readFile(filename));
		
		return dataList;
	}
	
	/**
	 * Read all data in a file
	 * @param filename
	 * @return
	 */
	private byte[] readFile(String filename){
		try {
			Path path = Paths.get("file:///" + dfsRootFolder + "/" + filename );
			return Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
