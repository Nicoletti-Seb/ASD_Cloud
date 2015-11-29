package fr.unice.miage.sd.tinydfs.nodes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
	public MasterImpl(int nbSlaves, String dfsRootFolder) throws RemoteException {
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
	 * @see fr.unice.miage.sd.tinydfs.nodes.Master#saveBytes(java.lang.String,
	 * byte[])
	 */
	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		List<byte[]> subFileContentRight = new ArrayList<>();
		List<byte[]> subFileContentLeft = new ArrayList<>();
		int sizePart = (int) (fileContent.length / slaves.length);
		int overflow = (int) (fileContent.length % slaves.length);
		byte[] buffer = null;

		for (byte indexPart = 0; indexPart < slaves.length; indexPart++) {
			if (indexPart == 0) {
				buffer = new byte[sizePart + overflow + 1];
				System.arraycopy(fileContent, 0, buffer, 1, sizePart + overflow);
			} else {
				buffer = new byte[sizePart + 1];
				int indexSrc = indexPart * sizePart;
				System.arraycopy(fileContent, indexSrc, buffer, 1, sizePart);
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

		if (subFileContentLeft.isEmpty() || subFileContentRight.isEmpty()) {
			return null;
		}

		// Save block file
		saveListBlockFile(filename, subFileContentLeft);
		saveListBlockFile(filename, subFileContentRight);

		// Build the file
		try {
			File file = new File(dfsRootFolder + "/" + filename);
			file.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

			for (byte indexPart = 0; indexPart < slaves.length; indexPart++) {
				byte[] data = readBlockFile(filename, indexPart);
				bos.write(data, 1, data.length - 1);
			}

			bos.close();
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Save data blocks
	 * 
	 * @param filename:
	 *            the file's name build with this blocks
	 * @param list:
	 *            the block list
	 */
	private void saveListBlockFile(String filename, List<byte[]> list) {
		for (byte[] block : list) {
			File file = new File(dfsRootFolder + "/" + filename + "-" + block[0]);
			saveBlockFile(file, block);
		}
	}

	/**
	 * Allow to save the data block in a file
	 * 
	 * @param file
	 * @param data
	 */
	private void saveBlockFile(File file, byte[] data) {
		BufferedOutputStream bos = null;
		try {

			if (!file.exists()) {
				file.createNewFile();
			}

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

	/**
	 * Read the file contain the data block at indexPart.
	 * 
	 * @param filename
	 * @param indexPart
	 * @return
	 */
	private byte[] readBlockFile(String filename, int indexPart) {
		try {
			File file = new File(dfsRootFolder + "/" + filename + "-" + indexPart);
			return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Master#retrieveBytes(java.lang.String)
	 */
	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		List<byte[]> subFileContentLeft = slaves[0].subRetrieve(filename);
		List<byte[]> subFileContentRight = slaves[1].subRetrieve(filename);

		if (subFileContentLeft.isEmpty() || subFileContentRight.isEmpty()) {
			return null;
		}

		// Save block file
		saveListBlockFile(filename, subFileContentLeft);
		saveListBlockFile(filename, subFileContentRight);

		// Build the file
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (byte indexPart = 0; indexPart < slaves.length; indexPart++) {
			byte[] data = readBlockFile(filename, indexPart);
			baos.write(data, 1, data.length - 1);
		}

		return baos.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.unice.miage.sd.tinydfs.nodes.Master#addSlave(fr.unice.miage.sd.tinydfs
	 * .nodes.Slave)
	 */
	@Override
	public boolean addSlave(Slave slave) throws RemoteException {
		int i = 0;
		for (; i < slaves.length; i++) {
			if (slaves[i] == null) {
				slaves[i] = slave;
				break;
			}
		}

		if (i == slaves.length) {
			return false;
		}

		// create the link with the parent
		if (i > 1) {
			int indexParent = (i >> 1) - 1; // (i / 2) -1
			if ((i & 1) == 0) { // i is divisible by 2
				slaves[indexParent].setLeftSlave(slave);
			} else {
				slaves[indexParent].setRightSlave(slave);
			}
		}

		return true;
	}
}
