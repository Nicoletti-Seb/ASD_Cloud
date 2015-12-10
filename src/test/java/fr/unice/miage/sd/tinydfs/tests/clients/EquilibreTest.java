package fr.unice.miage.sd.tinydfs.tests.clients;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import junitx.framework.FileAssert;
import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.tests.config.Constants;


public class EquilibreTest {

	private static String storageServiceName;
	private static String registryHost; 
	private static Master master;
	private static long testStartTime;

	@BeforeClass
	/**
	 * Reads the properties and sets up the master.
	 */
	public static void setUp() {
		testStartTime = System.currentTimeMillis();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(EquilibreTest.class.getResource(
					Constants.PROPERTIES_FILE_PATH).getFile());
			prop.load(input);
			storageServiceName = prop.getProperty(
					Constants.SERVICE_NAME_PROPERTY_KEY);
			registryHost = prop.getProperty(
					Constants.REGISTRY_HOST_PROPERTY_KEY);
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			if (input != null) {
				try {
					input.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			Registry registry = LocateRegistry.getRegistry(
					registryHost, Registry.REGISTRY_PORT);
			master = (Master) registry.lookup(storageServiceName);
		} 
		catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
			System.err.println("[ClientsTest] No master found, exiting.");
			System.exit(1);
		}
		try {
			Thread.sleep(500);
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	/**
	 * Sauvegarde de byte, verification equilibrage enregistrement 
	 */
	public void binaryClientTest() {
		File expectedFile = new File(this.getClass().getResource(
				Constants.BINARY_SAMPLE_FILE_PATH).getFile().substring(1));
		
		Path path = Paths.get(this.getClass().getResource(
				Constants.BINARY_SAMPLE_FILE_PATH).getFile().substring(1));
		BufferedOutputStream bos = null;
		File retrievedFile = null;

		try {
			byte[] data = Files.readAllBytes(path);
			master.saveBytes(Constants.BINARY_SAMPLE_FILE_NAME, data);
			//comparaison de la taille des fichiers enregistres (exccepte le dernier qui peut enregistrer les debordements)
			for (int i = 0; i<master.getNbSlaves()-2;i++){
				File f = new File(master.getDfsRootFolder(),i+2+Constants.TEXTUAL_SAMPLE_FILE_NAME);
				File f2 = new File(master.getDfsRootFolder(),i+3+Constants.TEXTUAL_SAMPLE_FILE_NAME);
				Assert.assertEquals(f.length(), f2.length(), 0);
			}
			
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (bos != null) {
				try {
					bos.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	/**
	 * Sauvegarde de fichier, verification equilibrage
	 */
	public void textualClientTest() {
		try {
			File expectedFile = new File(this.getClass().getResource(
					Constants.TEXTUAL_SAMPLE_FILE_PATH).getFile());
			master.saveFile(expectedFile);
			//comparaison de la taille des fichiers enregistres (exccepte le dernier qui peut enregistrer les debordements)
			for (int i = 0; i<master.getNbSlaves()-2;i++){
				File f = new File(master.getDfsRootFolder(),i+2+Constants.TEXTUAL_SAMPLE_FILE_NAME);
				File f2 = new File(master.getDfsRootFolder(),i+3+Constants.TEXTUAL_SAMPLE_FILE_NAME);
			
				Assert.assertEquals(f.length(), f2.length(), 0);
			}
			
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
