package fr.unice.miage.sd.tinydfs.tests.clients;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.tests.config.Constants;

public class FileBalancedTest {

	private static String storageServiceName;
	private static String registryHost;
	private static Master master;

	@BeforeClass
	/**
	 * Reads the properties and sets up the master.
	 */
	public static void setUp() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(FileBalancedTest.class.getResource(Constants.PROPERTIES_FILE_PATH).getFile());
			prop.load(input);
			storageServiceName = prop.getProperty(Constants.SERVICE_NAME_PROPERTY_KEY);
			registryHost = prop.getProperty(Constants.REGISTRY_HOST_PROPERTY_KEY);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			Registry registry = LocateRegistry.getRegistry(registryHost, Registry.REGISTRY_PORT);
			master = (Master) registry.lookup(storageServiceName);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
			System.err.println("[ClientsTest] No master found, exiting.");
			System.exit(1);
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	/**
	 * Saves bytes from a file through the master, and checks if this size with
	 * original file are equal.
	 */
	public void binaryFileSizeTest() {
		Path path = Paths.get(this.getClass().getResource(Constants.BINARY_SAMPLE_FILE_PATH).getPath().substring(1));

		try {
			byte[] data = Files.readAllBytes(path);
			master.saveBytes(Constants.BINARY_SAMPLE_FILE_NAME, data);

			Thread.sleep(300);

			File slaveFolder = new File(master.getDfsRootFolder() + "/../Slave/");
			List<File> fileList = searchFile(slaveFolder, Constants.BINARY_SAMPLE_FILE_NAME);
			Assert.assertEquals(master.getNbSlaves(), fileList.size());

			filesAreBalanced(data.length, fileList);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Test
	/**
	 * Saves bytes from a file through the master, and checks if this size with
	 * original file are equal.
	 */
	public void textualFileSizeTest() {
		try {
			File expectedFile = new File(this.getClass().getResource(Constants.TEXTUAL_SAMPLE_FILE_PATH).getFile());
			master.saveFile(expectedFile);

			Thread.sleep(300);

			File slaveFolder = new File(master.getDfsRootFolder() + "/../Slave/");
			List<File> fileList = searchFile(slaveFolder, Constants.TEXTUAL_SAMPLE_FILE_NAME);
			Assert.assertEquals(master.getNbSlaves(), fileList.size());

			filesAreBalanced((int) expectedFile.length(), fileList);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Test if the file list is balanced
	 * 
	 * @param sizeFile
	 * @param fileList
	 */
	private void filesAreBalanced(int sizeFile, List<File> fileList) {
		try {
			int sizeBlock = (sizeFile / master.getNbSlaves()) + 1;
			int overflow = sizeFile % master.getNbSlaves();
			int nbFile = 0;

			for (File f : fileList) {
				if (f.length() == sizeBlock) {
					nbFile++;
				}
			}

			if (overflow > 0) {
				Assert.assertEquals(master.getNbSlaves() - 1, nbFile);
			} else {
				Assert.assertEquals(master.getNbSlaves(), nbFile);
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Search all file with the name contain in filename.
	 * 
	 * @param filename
	 * @return
	 */
	private List<File> searchFile(File src, String filename) {
		if (!src.exists()) {
			return null;
		}

		List<File> fileList = new LinkedList<>();
		for (File f : src.listFiles()) {

			if (f.isDirectory()) {
				fileList.addAll(searchFile(f, filename));
			} else if (f.getName().equals(filename)) {
				fileList.add(f);
			}
		}

		return fileList;
	}

	/**
	 * Removes all files create during the test
	 */
	@After
	public void clean() {
		try {
			File folderMaster = new File(master.getDfsRootFolder());
			File folderSlave = new File(master.getDfsRootFolder() + "/../Slave/");

			cleanFolder(folderMaster);
			cleanFolder(folderSlave);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Remove all files in a directory
	 * 
	 * @param folder
	 */
	private void cleanFolder(File folder) {

		if (!folder.exists() || !folder.isDirectory()) {
			return;
		}

		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				cleanFolder(f);
			} else {
				f.delete();
			}
		}
	}

}
