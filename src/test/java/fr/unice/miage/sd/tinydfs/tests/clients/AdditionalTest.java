package fr.unice.miage.sd.tinydfs.tests.clients;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.unice.miage.sd.tinydfs.impl.Logger;
import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.tests.config.Constants;

public class AdditionalTest {

	private static String storageServiceName;
	private static String registryHost; 
	private static Master master;
	
	@Before
	public void setUp() throws Exception {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(ClientsTest.class.getResource(
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

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void binarySizeTest() {
		
		try {
			File expectedFile = new File(this.getClass().getResource(
					Constants.BINARY_SAMPLE_FILE_PATH).getFile());
			master.saveFile(expectedFile);
			long retrievedSize = master.getSize(Constants.BINARY_SAMPLE_FILE_NAME);
			
			Logger.getInstance().log("Binary Expected : " + expectedFile.length() + " Retrieved : " + retrievedSize);
			
			Assert.assertTrue(expectedFile.length() == retrievedSize);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void textualSizeTest() {
		
		try {
			File expectedFile = new File(this.getClass().getResource(
					Constants.TEXTUAL_SAMPLE_FILE_PATH).getFile());
			master.saveFile(expectedFile);
			long retrievedSize = master.getSize(Constants.TEXTUAL_SAMPLE_FILE_NAME);

			Logger.getInstance().log("Textual Expected : " + expectedFile.length() + " Retrieved : " + retrievedSize);

			Assert.assertTrue(expectedFile.length() == retrievedSize);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
