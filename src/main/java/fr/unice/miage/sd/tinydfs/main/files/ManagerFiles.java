package fr.unice.miage.sd.tinydfs.main.files;

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
import java.util.LinkedList;
import java.util.List;

public class ManagerFiles {

	private String dfsRootFolder;

	public ManagerFiles(String dfsRootFolder) {
		this.dfsRootFolder = dfsRootFolder;
	}

	/**
	 * Allow to save the data in a file with filename
	 * 
	 * @param file
	 * @param data
	 */
	public void saveFile(String filename, byte[] data) {
		try {
			File file = new File(dfsRootFolder + "/" + filename);
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			try {
				bos.write(data);
			} finally {
				bos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read all data in a file
	 * 
	 * @param filename
	 * @return
	 */
	public byte[] readFile(String filename) {
		try {
			File file = new File(dfsRootFolder + "/" + filename);
			return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Return the file's size.
	 * 
	 * @param filename
	 * @return
	 * @throws RemoteException
	 */
	public int getSizeFile(String filename) throws RemoteException {
		File file = new File(dfsRootFolder + "/" + filename);
		return (int) file.length();
	}

	// -------------------------------------- Methode to cut
	public List<byte[]> cutFile(File file, int nbPart) {
		List<byte[]> partList = new LinkedList<>();
		int sizePart = (int) (file.length() / nbPart);
		int overflow = (int) (file.length() % nbPart);
		byte[] buffer;

		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			try {
				for (byte indexPart = 0; bis.available() > 0; indexPart++) {
					if (indexPart == 0) {
						buffer = new byte[sizePart + overflow + 1];
						bis.read(buffer, 1, sizePart + overflow);
					} else {
						buffer = new byte[sizePart + 1];
						bis.read(buffer, 1, sizePart);
					}
					buffer[0] = indexPart;
					partList.add(buffer);
				}
			} finally {
				bis.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return partList;
	}

	public List<byte[]> cutByteArray(byte[] data, int nbPart) {
		List<byte[]> partList = new LinkedList<>();
		int sizePart = (int) (data.length / nbPart);
		int overflow = (int) (data.length % nbPart);
		byte[] buffer = null;

		for (byte indexPart = 0; indexPart < nbPart; indexPart++) {
			if (indexPart == 0) {
				buffer = new byte[sizePart + overflow + 1];
				System.arraycopy(data, 0, buffer, 1, sizePart + overflow);
			} else {
				buffer = new byte[sizePart + 1];
				int indexSrc = indexPart * sizePart + overflow;
				System.arraycopy(data, indexSrc, buffer, 1, sizePart);
			}

			buffer[0] = indexPart;
			partList.add(buffer);
		}

		return partList;
	}

	// -------------------------------------- Methode to build
	public File buildFile(String filename, int nbPart) {
		File file = null;
		try {
			file = new File(dfsRootFolder + "/" + filename);
			file.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

			try {
				for (byte indexPart = 0; indexPart < nbPart; indexPart++) {
					byte[] data = readFile(filename + "-" + indexPart);
					bos.write(data, 1, data.length - 1);
				}
			} finally {
				bos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}

	public byte[] buildByteArray(String filename, int nbPart) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		for (byte indexPart = 0; indexPart < nbPart; indexPart++) {
			byte[] data = readFile(filename + "-" + indexPart);
			baos.write(data, 1, data.length - 1);
		}

		return baos.toByteArray();
	}

	// -------------------------------------- Getter
	public String getDfsRootFolder() {
		return dfsRootFolder;
	}

}
