package edu.spam.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import edu.spam.framework.filters.Filter;

public class Runner {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws MessagingException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws FileNotFoundException, MessagingException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		
		Class[] filters = getClasses("edu.spam.framework.filters");
		
		for (Class c : filters) {
			Filter filter = (Filter)c.newInstance();
			
			Scanner trainInput = new Scanner(new File("trec06p/full/index"));
			while (trainInput.hasNextLine()) {
				String line = trainInput.nextLine();
				String[] parts = line.split(" ");
				File dataFile = new File("trec06p/full", parts[1]);
				MimeMessage message = MessageParser.parseMessage(dataFile.getAbsolutePath());
				filter.train(message);
				System.out.println(message.getContentType() + " " + message.getSubject());
			}
			trainInput.close();
			
			// we should download trec07 for the test data  it'll be similar training method.
		}
	}
	
	/**
	* list Classes inside a given package
	* @author Jon Peck http://jonpeck.com (adapted from http://www.javaworld.com/javaworld/javatips/jw-javatip113.html)
	* @param pckgname String name of a Package, EG "java.lang"
	* @return Class[] classes inside the root of the given package
	* @throws ClassNotFoundException if the Package is invalid
	*/
	public static Class[] getClasses(String pckgname)
			throws ClassNotFoundException {
		ArrayList<Class> classes = new ArrayList<Class>();
		// Get a File object for the package
		File directory = null;
		try {
			directory = new File(Thread.currentThread().getContextClassLoader().getResource(pckgname.replace('.', '/')).getFile());
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be a valid package");
		}
		if (directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					classes.add(Class.forName(pckgname + '.'
							+ files[i].substring(0, files[i].length() - 6)));
				}
			}
		} else {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be a valid package");
		}
		Class[] classesA = new Class[classes.size()];
		classes.toArray(classesA);
		return classesA;
	} 

}
