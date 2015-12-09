package fr.unice.miage.sd.tinydfs.impl;

import java.io.File;
import java.io.FileOutputStream;
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

	private static final long serialVersionUID = 7884543114695525732L;
	
	private String masterHost;
	private String dfsRootFolder;
	private int slaveId;

	private Slave slaveLeft = null;
	private Slave slaveRight = null;

	public SlaveImpl(String masterHost, String dfsRootFolder, int slaveId) throws RemoteException {
		super();
		this.masterHost = masterHost;
		this.dfsRootFolder = dfsRootFolder;
		this.slaveId = slaveId;

	}
	
	/*
	 * Binding des Slaves
	 * Récupération du slave de droite et de gauche à l'aide de la formule 
	 * SlaveLeft = 2*idSlave + 2 ; SlaveRight = 2*idSlave + 3
	 */
	@Override
	public void bind() throws RemoteException {
		
		try {

			try {
				slaveLeft = (Slave) Naming.lookup("rmi://" + SlaveImpl.this.masterHost + "/" + "slave"+(2*SlaveImpl.this.slaveId +2));
				
				// On rebind récursivement si il existe un Slave fils
				//
				slaveLeft.bind();
			} catch(NotBoundException e) {
				// Commentaire du PrintStackTrace puis l'erreur est attendue et gérée pour ne pas surcharger la console.
				//e.printStackTrace();
				slaveLeft = null;
			}

			try {
				slaveRight = (Slave) Naming.lookup("rmi://" + SlaveImpl.this.masterHost + "/" + "slave"+(2*SlaveImpl.this.slaveId + 3));
				
				// On rebind récursivement si il existe un Slave fils
				//
				slaveRight.bind();
			} catch(NotBoundException e) {
				// Commentaire du PrintStackTrace puis l'erreur est attendue et gérée pour ne pas surcharger la console.
				//e.printStackTrace();
				slaveRight = null;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (RemoteException e) {
			e.printStackTrace();
		}
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

	/*
	 * Sauvegarde des tableau de byte de façon récursive et ordonnée.
	 */
	@Override
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException {

		// Sauvegarde puis suppression du premier élément de la liste
		//
		saveByteArray(filename, subFileContent.get(0));
		subFileContent.remove(0);

		// Si la liste contient au moins 2 éléments.
		// Le cas où la liste ne contient qu'un seul élément ne peut se produire dans ce cas puisque nous travaillons sur des arbres binaires
		// où chaque niveau est complet.
		//
		if(subFileContent.size() > 1) {

			// Découpage de la liste en 2 parties égales
			//
			List<byte[]> leftList = new LinkedList<byte[]>(subFileContent.subList(0, subFileContent.size()/2));
			List<byte[]> rightList = new LinkedList<byte[]>(subFileContent.subList(subFileContent.size()/2, subFileContent.size()));

			// Sauvegarde des morceaux de liste
			//
			if(getLeftSlave() != null && getRightSlave() != null) {
				getLeftSlave().subSave(filename, leftList);
				getRightSlave().subSave(filename, rightList);
			}

		}

	}

	/*
	 * Récupérer de façon récursive les morceau du fichier demandé
	 */
	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {

		List<byte[]> result = new LinkedList<byte[]>();

		// Ajout du morceau de fichier du Slave courrant
		//
		result.add(getByteArray(filename));
		
		// Ajout des morceaux de fichier du slave de gauche et droite
		// Tant que le Slave courrant a des sous Slave.
		//
		if(getLeftSlave() != null && getRightSlave() != null) {
			result.addAll(getLeftSlave().subRetrieve(filename));
			result.addAll(getRightSlave().subRetrieve(filename));
		}

		return result.contains(null) ? null : result;
	}

	/*
	 * Récupérer un tableau de byte depuis un nom de fichier
	 */
	private byte[] getByteArray(String filename) {

		// Récupération du fichier concerné
		//
		File f = new File(dfsRootFolder, filename + slaveId);

		// Si le fichier existe on retourne son contenu
		//
		if(f.exists()) {
			try {
				return Files.readAllBytes(f.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return null;

	}

	/*
	 * Sauvegarde d'un tableau de byte dans un fichier
	 */
	private void saveByteArray(String filename, byte[] content) {

		// Récupération du fichier concerné
		//
		File f = new File(dfsRootFolder, filename + slaveId);

		// Si le fichier existe on le supprime
		// La dernière sauvegarde ayant la priorité
		//
		if(f.exists()) f.delete();

		// Ecriture du contenu sur le fichier
		//
		try {
			Files.write(f.toPath(), content, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	/*
	 * Récupérer la taille d'un fichier
	 */
	@Override
	public long getSize(String filename) throws RemoteException {

		List<byte[]> file = subRetrieve(filename);

		long result = 0;
		for(byte[] bArray : file) {
			result += bArray.length;
		}

		return result;
	}

}
