package fr.unice.miage.sd.tinydfs.imp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import fr.unice.miage.sd.tinydfs.nodes.Master;

public class MasterImp extends UnicastRemoteObject implements Master {

	//chemin RMI
	private String nameDirectorySave;
	//chemin rep 
	private String dfsRootFolder;
	private int nbSlave;
	//2 slave a recuperer grace au RMI
	
	public MasterImp( String dRF,String nDS, int nS) throws RemoteException {
		super();
		dfsRootFolder = dRF;
		nameDirectorySave = nDS;
		nbSlave = nS;
	}


	
	@Override
	public String getDfsRootFolder() throws RemoteException {
		String resultat = this.dfsRootFolder;
		return resultat;
	}

	@Override
	public int getNbSlaves() throws RemoteException {
		int resultat = this.nbSlave;
		return resultat;
	}

	@Override
	public void saveFile(File file) throws RemoteException {
		List<byte[]> subFileContent = new ArrayList<byte[]>();
		//lit le fichier et rempli un tableau de byte
		byte[] total = null ;
		if(file.exists()){
			try {
				//tout le fichier dans le tableau de byte
				total = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//creer le fichier 
		File f = new File(this.nameDirectorySave+File.separator+"test_"+file.getName());
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//si taille fichier non null 
		if (total != null){
			//divise le nombre de byte total du fichier, arrondi en dessous 
			int tailleTableau = Math.floorDiv(total.length,this.nbSlave);
			//boucle sur le nombre de slave-1 (le dernier est traite a part)
			for(int i=0;i<this.nbSlave-1;i++){
				//creation du tableau 
				byte[] tableauListe = new byte[tailleTableau];
				//boucle sur le tableau pour le remplir
				for(int j=0;j<tailleTableau;j++){
					//rempli le tableau avec le bon indice tu tableau contenant tous les bytes
					tableauListe[j]=total[j+(i*tailleTableau)];	
				}	
				subFileContent.add(tableauListe);
			}
			//dernier tableau avec la taille (qui peut etre superieur dans certains cas de partage )
			byte[] tableauListe = new byte[total.length-((this.nbSlave-1)*tailleTableau)];
			//remplissage du tableau
			for(int j=0;j<tableauListe.length;j++){
				tableauListe[j]=total[j+((this.nbSlave-1)*tailleTableau)];	
				System.out.println(j+((this.nbSlave-1)*tailleTableau));
			}	
			subFileContent.add(tableauListe);
			
			//liste separe en 2 
			List<byte[]> subFileContentDroit = subFileContent.subList(0, (subFileContent.size()/2)-1);
			List<byte[]> subFileContentGauche = subFileContent.subList(subFileContent.size()/2,subFileContent.size()-1);
			//envoie des 2 listes aux slaves
			
		}
		
	}

	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		List<byte[]> subFileContent = new ArrayList<byte[]>();
		//creer le fichier 
		File f = new File(this.nameDirectorySave+File.separator+"test_"+filename);
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//divise le nombre de byte total du fichier, arrondi en dessous 
		int tailleTableau = Math.floorDiv(fileContent.length,this.nbSlave);
		//boucle sur le nombre de slave-1 (le dernier est traite a part)
		for(int i=0;i<this.nbSlave-1;i++){
			//creation du tableau 
			byte[] tableauListe = new byte[tailleTableau];
			//boucle sur le tableau pour le remplir
			for(int j=0;j<tailleTableau;j++){
				//rempli le tableau avec le bon indice tu tableau contenant tous les bytes
				tableauListe[j]=fileContent[j+(i*tailleTableau)];	
			}	
			subFileContent.add(tableauListe);
		}
		//dernier tableau avec la taille (qui peut etre superieur dans certains cas de partage )
		byte[] tableauListe = new byte[fileContent.length-((this.nbSlave-1)*tailleTableau)];
		//remplissage du tableau
		for(int j=0;j<tableauListe.length;j++){
			tableauListe[j]=fileContent[j+((this.nbSlave-1)*tailleTableau)];	
			System.out.println(j+((this.nbSlave-1)*tailleTableau));
		}	
		subFileContent.add(tableauListe);
		
		//liste separe en 2 
		List<byte[]> subFileContentDroit = subFileContent.subList(0, (subFileContent.size()/2)-1);
		List<byte[]> subFileContentGauche = subFileContent.subList(subFileContent.size()/2,subFileContent.size()-1);
		//envoie des 2 listes aux slaves
	}

	@Override
	public File retrieveFile(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
}
