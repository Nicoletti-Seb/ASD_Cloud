package fr.unice.miage.sd.tinydfs.imp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.unice.miage.sd.tinydfs.nodes.BufferInterface;
import fr.unice.miage.sd.tinydfs.nodes.Master;
import fr.unice.miage.sd.tinydfs.nodes.Slave;


public class MasterImp extends UnicastRemoteObject implements Master {

	//chemin RMI
	private String storageServiceName;
	//chemin rep 
	private String dfsRootFolder;
	private int nbSlave;
	//2 slave a recuperer grace au RMI
	private Slave filsG;
	private Slave filsD;
	private BufferInterface bI;
	private List fichierUtilise = new ArrayList<>();
	public MasterImp( String sSN,String dRF, int nS) throws RemoteException {
		super();
		storageServiceName = sSN;
		dfsRootFolder = dRF;
		nbSlave = nS;
		try {
			bI = (BufferInterface)Naming.lookup("rmi://localhost/Controleur");
			construire();
		} catch (MalformedURLException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void construire() throws RemoteException{
		
		Runnable run = new Runnable() {
			@Override
			public void run() {
				try {
					//attend que les slave soit inscrit
					bI.ajoutFini();
					constructionArbre();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};
		//lancement du run
		Thread t = new Thread(run);
		t.start();
	}
	
	public void constructionArbre(){
		for (int i = 0;i<this.nbSlave;i++){
			int index = i+2;
			//premier slave
			if(i==0){
				try {
					this.filsG = (Slave)Naming.lookup("rmi://localhost/slave_"+index);
				} catch (MalformedURLException | RemoteException
						| NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//deuxieme slave
			else if(i==1){
				try {
					this.filsD = (Slave)Naming.lookup("rmi://localhost/slave_"+index);
				} catch (MalformedURLException | RemoteException
						| NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//arbre binare mis en relation
			else {
				if(i%2==0){
					try {
						Slave pere = (Slave)Naming.lookup("rmi://localhost/slave_"+index/2)	;
						pere.setLeftSlave((Slave)Naming.lookup("rmi://localhost/slave_"+index));
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					Slave pere;
					try {
						pere = (Slave)Naming.lookup("rmi://localhost/slave_"+(index-1)/2);
						pere.setRightSlave((Slave)Naming.lookup("rmi://localhost/slave_"+index));
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		}
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
		final File f = file;
		fichierUtilise.add(f.getName());
		//pour que ce ne soit pas bloquant 
		new Thread(new Runnable(){
			@Override
			public void run() {
				//ajout fichier a la liste, exclusivite sur utilisation
				
				List<byte[]> subFileContent = new ArrayList<byte[]>();
				//lit le fichier et rempli un tableau de byte
				byte[] total = null ;
				if(f.exists()){
					try {
						//tout le fichier dans le tableau de byte
						total = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//si taille fichier non null 
				if (total != null){
					//divise le nombre de byte total du fichier, arrondi en dessous 
					int tailleTableau = Math.floorDiv(total.length,nbSlave);
					//boucle sur le nombre de slave-1 (le dernier est traite a part)
					for(int i=0;i<nbSlave-1;i++){
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
					byte[] tableauListe = new byte[total.length-((nbSlave-1)*tailleTableau)];
					//remplissage du tableau
					for(int j=0;j<tableauListe.length;j++){
						tableauListe[j]=total[j+((nbSlave-1)*tailleTableau)];	
					}	
					subFileContent.add(tableauListe);
					
					//liste separe en 2 
					List<byte[]> subFileContentGauche =new LinkedList(subFileContent.subList(0, (subFileContent.size()/2)));
					List<byte[]> subFileContentDroit =new LinkedList(subFileContent.subList(subFileContent.size()/2,subFileContent.size()));
					//envoie des 2 listes aux slaves
					try {
						filsG.subSave(f.getName(), subFileContentGauche);
						filsD.subSave(f.getName(), subFileContentDroit);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
				//suppression du fichier utlise de la liste afin de permettre l'acces
				fichierUtilise.remove(fichierUtilise.indexOf(f.getName()));
				synchronized (MasterImp.this) {
					MasterImp.this.notifyAll();
				}
			}	
		}).start();
	}

	@Override
	public void saveBytes(String filename, byte[] fileContent) throws RemoteException {
		final byte[] fC = fileContent.clone();
		final String fLN = filename;
		fichierUtilise.add(fLN);
		new Thread(new Runnable() {
			@Override
			public void run() {
				//ajout fichier a la liste, exclusivite sur utilisation
				
				List<byte[]> subFileContent = new ArrayList<byte[]>();
				//divise le nombre de byte total du fichier, arrondi en dessous 
				int tailleTableau = Math.floorDiv(fC.length,nbSlave);
				//boucle sur le nombre de slave-1 (le dernier est traite a part)
				for(int i=0;i<nbSlave-1;i++){
					//creation du tableau 
					byte[] tableauListe = new byte[tailleTableau];
					//boucle sur le tableau pour le remplir
					for(int j=0;j<tailleTableau;j++){
						//rempli le tableau avec le bon indice tu tableau contenant tous les bytes
						tableauListe[j]=fC[j+(i*tailleTableau)];	
					}	
					subFileContent.add(tableauListe);
				}
				//dernier tableau avec la taille (qui peut etre superieur dans certains cas de partage )
				byte[] tableauListe = new byte[fC.length-((nbSlave-1)*tailleTableau)];
				//remplissage du tableau
				for(int j=0;j<tableauListe.length;j++){
					tableauListe[j]=fC[j+((nbSlave-1)*tailleTableau)];	
				}	
				subFileContent.add(tableauListe);
				//liste separe en 2 
				List<byte[]> subFileContentGauche = new LinkedList<byte[]>(subFileContent.subList(0, subFileContent.size() / 2));
				List<byte[]> subFileContentDroit = new LinkedList<byte[]>(subFileContent.subList(subFileContent.size()/2,subFileContent.size()));
			
				
				//envoie des 2 listes aux slaves
				try {
					filsG.subSave(fLN, subFileContentGauche);
					filsD.subSave(fLN, subFileContentDroit);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//suppression du fichier utlise de la liste afin de permettre l'acces
				fichierUtilise.remove(fichierUtilise.indexOf(fLN));
				
				synchronized (MasterImp.this) {
					MasterImp.this.notifyAll();
				}
				
			}
		}).start();
		
		
	}

	@Override
	public File retrieveFile(String filename) throws RemoteException {
		//attend fin save
		fichierNonPartage(filename);
		// appel des slave, 2 listes a faire
		List<byte[]> totalGauche = this.filsG.subRetrieve(filename);
		List<byte[]> totalDroit = this.filsD.subRetrieve(filename);

		//creation fichier, peut etre ameliorer
		File fichier = new File(this.dfsRootFolder +File.separator+filename);
		//si le fichier existe deja 
		if(fichier.exists()){
			//suppression ancien fichier
			fichier.delete();
			try {
				//nouveau fichier cree
				fichier.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
		
		//ecrire dans le fichier cree precedemment
		try {
			FileOutputStream f = new FileOutputStream(fichier);
			for (byte[] tab : totalGauche){
				f.write(tab);
			}
			for (byte[] tab : totalDroit){
				f.write(tab);
			}
			f.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return fichier;
	}

	@Override
	public byte[] retrieveBytes(String filename) throws RemoteException {
		//atend fin save
		fichierNonPartage(filename);
		// appel des slave, 2 listes a faire
		List<byte[]> totalGauche = filsG.subRetrieve(filename);
		List<byte[]> totalDroit = filsD.subRetrieve(filename);
		
		
		//tableau de bon 
		if(totalDroit!= null && totalDroit.size()>0){
			//tableau de byte
			byte[] resultat = new byte[(totalDroit.size()*2)*(totalDroit.get(totalDroit.size()-1).length)];
			
			int depart = 0;
			for(int i=0;i<totalGauche.size();i++){
				System.arraycopy(totalGauche.get(i), 0, resultat, depart, totalGauche.get(i).length);
				//position de depart de la destination incremente en fonction de la taille des tableaux ajoutés
				depart += totalGauche.get(i).length;
			}
			
			for(int i=0;i<totalDroit.size();i++){
				System.arraycopy(totalDroit.get(i), 0, resultat, depart, totalDroit.get(i).length);
				//position de depart de la destination incremente en fonction de la taille des tableaux ajoutés
				depart += totalDroit.get(i).length;
			}
			return resultat;
		}
		
		return null;
	}


	//taille du fichier
	public float getSize(String fileName) throws RemoteException {
		//attend fin save
		fichierNonPartage(fileName);
		
		float resultat = this.filsD.getSize(fileName) + this.filsG.getSize(fileName);
		return resultat;
	}



	@Override
	public void setLeftSlave(Slave slave) throws RemoteException {
		this.filsG=slave;
		
	}



	@Override
	public void setRightSlave(Slave slave) throws RemoteException {
		this.filsD=slave;
	}
	
	//verification pas utilisation par methode de sauvegarde 
	private synchronized void fichierNonPartage(String filename) {
		try {
			while (fichierUtilise.contains(filename)) {
				wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
