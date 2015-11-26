package fr.unice.miage.sd.tinydfs.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

// Classe Logger
// Classe de service pour logger des évènements dans un fichier texte.
//
public class Logger {

	// Utilisation du design patern Singleton
	//
	private static Logger instance;

	private static File file;

	private Logger() {

		file = new File(".", "log.txt");
		
		if(!file.exists()) {
			try {
				Files.createFile(file.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		

	}

	public static Logger getInstance() {			
		if (instance == null) { 	
			instance = new Logger();	
		}
		return instance;
	}

	/*
	 * Méthode log
	 * Ecrire une chaine de caractère dans le fichier de log
	 */
	public void log(String toLog) {

		try {
			Files.write(file.toPath(), (toLog + "\n").getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
