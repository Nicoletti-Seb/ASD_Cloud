package fr.unice.miage.sd.tinydfs.imp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.nodes.Slave;

public class SlaveImp extends UnicastRemoteObject implements Slave{
	private String masterHost;
	private String dfs_root_folder;
	private int id;
	private Slave filsG;
	private Slave filsD;
	
	
	public SlaveImp(String mH, String drf, int i) throws RemoteException {
		super();
		masterHost=mH;
		dfs_root_folder = drf;
		id = i+2;
	}
	

	@Override
	public int getId() throws RemoteException {
		int res = this.id;
		return res;
	}

	@Override
	public Slave getLeftSlave() throws RemoteException {
		Slave sI = this.filsG;
		return this.filsG;
	}

	@Override
	public Slave getRightSlave() throws RemoteException {
		Slave sI = this.filsD;
		return sI;
	}

	@Override
	public void setLeftSlave(Slave slave) throws RemoteException {
		this.filsG = slave;
	}

	@Override
	public void setRightSlave(Slave slave) throws RemoteException {
		this.filsD = slave;
	}

	@Override
	public void subSave(String filename, List<byte[]> subFileContent) throws RemoteException {
		
		//creer le fichier 
		File f = new File(this.dfs_root_folder,this.id+filename);
		
		if(f.exists()){
			f.delete();
		}
				
		try {
			//ecriture dans le fichier
			Files.write(f.toPath(), subFileContent.get(0), StandardOpenOption.CREATE);
		
			//si a des slave
			if(subFileContent.size()>1){
				subFileContent.remove(0);
				//division de la liste
				List<byte[]> subFileContentGauche =  new LinkedList<byte[]>(subFileContent.subList(0, subFileContent.size()/2));
				List<byte[]> subFileContentDroit = new LinkedList<byte[]>(subFileContent.subList(subFileContent.size()/2, subFileContent.size()));
				//appel des deux slave
				this.filsG.subSave(filename, subFileContentGauche);
				this.filsD.subSave(filename, subFileContentDroit);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<byte[]> subRetrieve(String filename) throws RemoteException {
		
		List<byte[]> result = new LinkedList<byte[]>();

		// Ajout du morceau de fichier du Slave courrant
		result.add(getByteArray(filename));
		
		//recupere list byte fils droit si existe
		if(this.filsG!=null){
			result.addAll(getLeftSlave().subRetrieve(filename));
			result.addAll(getRightSlave().subRetrieve(filename));
			
		}
		
		return result;
	}

	/*
	 * Récupérer un tableau de byte depuis un nom de fichier
	 */
	private byte[] getByteArray(String filename) {

		// Récupération du fichier concerné
		//
		File f = new File(dfs_root_folder, this.id + filename );
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

	@Override
	public float getSize(String filename) throws RemoteException {
		float taille = 0;
		File f = new File(dfs_root_folder, this.id + filename );
		if(f.exists()){
			taille =  f.length();
		}
		if(this.filsD!=null){
			taille += this.filsG.getSize(filename);
			taille += this.filsD.getSize(filename);
		}
		return taille;
	}
}
