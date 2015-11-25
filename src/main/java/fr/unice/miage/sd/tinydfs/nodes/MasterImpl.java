package fr.unice.miage.sd.tinydfs.nodes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class MasterImpl.
 * 
 * @author Sébatien Nicoletti
 */
public class MasterImpl extends UnicastRemoteObject implements Master {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Slave[] slaves;

	private String dfsRootFolder;

	/**
	 * Instantiates a new master impl.
	 *
	 * @throws RemoteException
	 *             the remote exception
	 */
	protected MasterImpl(int nbSlaves, String dfsRootFolder) throws RemoteException {
		super();
		this.slaves = new Slave[nbSlaves];
		this.dfsRootFolder = dfsRootFolder;
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#getDfsRootFolder()
	 */
	@Override
	public String getDfsRootFolder() throws RemoteException {
		return dfsRootFolder;
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#getNbSlaves()
	 */
	@Override
	public int getNbSlaves() throws RemoteException {
		return slaves.length;
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#saveFile(java.io.File)
	 * 
	 */
	@Override
	public void saveFile(File file) throws RemoteException {
		List<byte[]> subFileContentRight = new ArrayList<>();
		List<byte[]> subFileContentLeft = new ArrayList<>();
		int sizePart = (int) (file.length() / slaves.length);
		int overflow = (int) (file.length() % slaves.length);
		BufferedInputStream bis = null;
		byte[] buffer;
		
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			for (byte indexPart = 0; bis.available() > 0; indexPart++) {
				if (indexPart == 0) {
					buffer = new byte[sizePart + overflow + 1];
					bis.read(buffer, 1, sizePart + overflow);
				} else {
					buffer = new byte[sizePart + 1];
					bis.read(buffer, 1, sizePart);
				}
				buffer[0] = indexPart;

				if (indexPart % 2 == 0) {
					subFileContentLeft.add(buffer);
				} else {
					subFileContentRight.add(buffer);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		slaves[0].subSave(file.getName(), subFileContentLeft);
		slaves[1].subSave(file.getName(), subFileContentRight);
	}

	/*
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#saveBytes(java.lang.String, byte[])
	 */
	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		List<byte[]> subFileContentRight = new ArrayList<>();
		List<byte[]> subFileContentLeft = new ArrayList<>();
		int sizePart = (int) (fileContent.length / slaves.length);
		int overflow = (int) (fileContent.length % slaves.length);
		byte [] buffer = null;
		
		for (byte indexPart = 0; indexPart > slaves.length; indexPart++) {
			if( indexPart == 0 ){
				buffer = new byte [sizePart + overflow + 1];
				System.arraycopy(fileContent, 0, buffer, 0, sizePart + overflow);
			}
			else{
				buffer = new byte[sizePart + 1 ];
				int indexSrc = indexPart * sizePart;
				int indexDest = indexSrc + indexPart + 1;
				System.arraycopy(fileContent, indexSrc, buffer, indexDest, sizePart);
			}
			
			buffer[0] = indexPart;
			
			if (indexPart % 2 == 0) {
				subFileContentLeft.add(buffer);
			} else {
				subFileContentRight.add(buffer);
			}
			
		}
		
		slaves[0].subSave(filename, subFileContentLeft);
		slaves[1].subSave(filename, subFileContentRight);
	}

	/*
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Master#retrieveFile(java.lang.String)
	 */
	@Override
	public File retrieveFile(String filename) throws RemoteException {
		List<byte[]> subFileContentLeft = slaves[0].subRetrieve(filename);
		List<byte[]> subFileContentRight = slaves[1].subRetrieve(filename);
		
		if( subFileContentLeft.isEmpty() || subFileContentRight.isEmpty() ){
			return null;
		}
		
		try {
			File file = new File( dfsRootFolder + "/" + filename);
			file.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			
			for( byte indexPart = 0; indexPart < slaves.length; indexPart++){
				byte[] data = null;
				if( indexPart % 2 == 0 ){
					data = popFileBlockAtIndex(indexPart, subFileContentLeft);
				}
				else{
					data = popFileBlockAtIndex(indexPart, subFileContentRight);
				}
				bos.write(data, 1, data.length);
			}
			
			bos.close();
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Search and pop the memory block with index 'indexPart'
	 * @param indexPart
	 * @param list
	 * @return the memory block with indexPart, else null
	 */
	private byte[] popFileBlockAtIndex(byte indexPart, List<byte[]> list){
		Iterator<byte[]> i = list.iterator();
		
		while(i.hasNext()){
			byte []block = i.next();
			if( block[0] == indexPart ){
				i.remove();
				return block;
			}
		}
		
		return null;
	}

	/*
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Master#retrieveBytes(java.lang.String)
	 */
	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
