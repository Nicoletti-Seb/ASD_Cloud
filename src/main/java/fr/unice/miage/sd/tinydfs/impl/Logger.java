package fr.unice.miage.sd.tinydfs.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class Logger {

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

	public void log(String toLog) {

		try {
			Files.write(file.toPath(), (toLog + "\n").getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PrintStream getPrintStream() {
		try {
			return new PrintStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
