package fr.unice.miage.sd.tinydfs.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.nodes.Slave;


public class MasterImpl extends UnicastRemoteObject implements Master {

	private static final long serialVersionUID = -1855955039831240161L;

	private String storageServiceName;
	private String rootFolder;
	private int nbSlaves;

	// Références sur le Slave direct de gauche et le slave direct de droite
	//
	private Slave slaveLeft = null;
	private Slave slaveRight = null;

	public MasterImpl(String storageServiceName, String rootFoler, int nbSlaves) throws RemoteException {
		super();
		this.storageServiceName = storageServiceName;
		this.rootFolder = rootFoler;
		this.nbSlaves = nbSlaves;

		// Thread qui attend que tous les Slaves soient crées et ajoutés au RMI
		// Puis récupère leur référence
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					Thread.sleep(3500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				initSlaves();
			}
		}).start();
	}

	@Override
	public String getDfsRootFolder() throws RemoteException {
		return rootFolder;
	}

	@Override
	public int getNbSlaves() throws RemoteException {
		return nbSlaves;
	}

	/*
	 * Récupération de la référence des deux Slave gauche et droite.
	 */
	private void initSlaves() {

		if(slaveLeft != null && slaveRight != null) return;

		// Récupération des deux slaves par leur id, à savoir 0 et 1 pour les deux premier.
		try {
			slaveLeft = (Slave) Naming.lookup("rmi://localhost/slave0");
			slaveRight = (Slave) Naming.lookup("rmi://localhost/slave1");
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Division d'un tableau de byte en une liste de tableau de byte.
	 * La taille de la liste correspond au nombre de Slaves.
	 */
	private List<byte[]> splitByteArray(byte[] array) {

		List<byte[]> result = new LinkedList<byte[]>();

		int diff = array.length % nbSlaves;
		int lengthOfPart = (array.length - diff) / nbSlaves;
		int currentIndex = 0;

		for(int i = 0; i < nbSlaves; i++) {

			if(diff > 0) {
				
				result.add(Arrays.copyOfRange(array, currentIndex, currentIndex + lengthOfPart + 1));
				diff--;
				currentIndex += lengthOfPart + 1;
				
			} else {
				
				result.add(Arrays.copyOfRange(array, currentIndex, currentIndex + lengthOfPart));
				currentIndex += lengthOfPart;
				
			}
			
		}

		return result;

	}

	@Override
	public void saveFile(File file) throws RemoteException {

		try {
			List<byte[]> bytes = splitByteArray(Files.readAllBytes(file.toPath()));
			saveList(file.getName(), bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {

		List<byte[]> bytes = splitByteArray(fileContent);
		saveList(filename, bytes);
	}

	/*
	 * Sauvegarde d'une liste de tableau de byte par les Slaves.
	 */
	private void saveList(String filename, List<byte[]> bytes) {

		// Divison de la liste de tableau de byte en 2 parties égales
		//
		List<byte[]> leftList = new LinkedList<byte[]>(bytes.subList(0, bytes.size() / 2));
		List<byte[]> rightList = new LinkedList<byte[]>(bytes.subList(bytes.size()/2,bytes.size()));

		// Sauvegarde de chaque liste par un Slave
		try {
			slaveLeft.subSave(filename, leftList);
			slaveRight.subSave(filename, rightList);
			

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public File retrieveFile(String filename) throws RemoteException {

		// Récupération de la liste des tableau de byte
		//
		List<byte[]> totalList = getTotalList(filename);

		// Contrôle d'uniformité de la liste
		// Retour null si la liste n'est pas complète
		//
		if(totalList == null) return null;

		// Ecriture des bytes dans un fichier pour pouvoir le renvoyer.
		//
		File f = new File(rootFolder, filename);
		if(f.exists()) f.delete();

		try {
			Files.write(f.toPath(), getByteArrayFromList(totalList), StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return f;
	}

	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {

		// Récupération de la liste des tableau de byte
		//
		List<byte[]> totalList = getTotalList(filename);

		// Contrôle d'uniformité de la liste
		// Retour null si la liste n'est pas complète
		//
		if(totalList == null) return null;

		return getByteArrayFromList(totalList);
	}

	private List<byte[]> getTotalList(String filename) throws RemoteException {

		// Récupération des deux parties de la liste.
		// Liste gauche par le Slave de gauche et liste droite par le Slave de droite
		//
		List<byte[]> leftList = slaveLeft.subRetrieve(filename);
		List<byte[]> rightList = slaveRight.subRetrieve(filename);

		// Contrôle d'uniformité de la liste
		// Retour null si la liste n'est pas complète
		//
		if(leftList == null || rightList == null) return null;

		List<byte[]> totalList = new LinkedList<byte[]>();
		totalList.addAll(leftList);
		totalList.addAll(rightList);

		return totalList;
	}

	/*
	 * Comptage de la taille des byte d'une liste de tableau de byte
	 */
	private int getSizeOfList(List<byte[]> bytes) {

		int result = 0;

		for(byte[] byteArray : bytes) {
			result += byteArray.length;
		}
		
		return result;

	}

	/*
	 * Transformation d'une liste de tableau de byte en un tableau de byte
	 */
	private byte[] getByteArrayFromList(List<byte[]> bytes) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream(getSizeOfList(bytes));
		
		for(byte[] byteArray : bytes) {
			baos.write(byteArray, 0, byteArray.length);
		}

		return baos.toByteArray();

	}

	@Override
	public long getSize(String filename) throws RemoteException {
		
		return slaveLeft.getSize(filename) + slaveRight.getSize(filename);
	}

	@Override
	public List<byte[]> retrieveParts(String filename) throws RemoteException {
		
		return getTotalList(filename);
	}

}
