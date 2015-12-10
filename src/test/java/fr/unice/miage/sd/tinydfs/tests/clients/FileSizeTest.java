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
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.tests.config.Constants;

public class FileSizeTest {

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
			input = new FileInputStream(FileSizeTest.class.getResource(Constants.PROPERTIES_FILE_PATH).getFile());
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
		Path path = Paths.get(this.getClass().getResource(Constants.BINARY_SAMPLE_FILE_PATH).getPath());
		int retrieveSizeFile = 0;
		int expectedSizeFile = 0;

		try {
			byte[] data = Files.readAllBytes(path);
			expectedSizeFile = data.length;
			master.saveBytes(Constants.BINARY_SAMPLE_FILE_NAME, data);
			retrieveSizeFile = master.getSizeFile(Constants.BINARY_SAMPLE_FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(expectedSizeFile, retrieveSizeFile);
	}

	@Test
	/**
	 * Saves bytes from a file through the master, and checks if this size with
	 * original file are equal.
	 */
	public void textualFileSizeTest() {
		int retrieveSizeFile = 0;
		int expectedSizeFile = 0;

		try {
			File expectedFile = new File(this.getClass().getResource(Constants.TEXTUAL_SAMPLE_FILE_PATH).getFile());
			expectedSizeFile = (int) expectedFile.length();

			master.saveFile(expectedFile);

			retrieveSizeFile = master.getSizeFile(expectedFile.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(expectedSizeFile, retrieveSizeFile);
	}
}
