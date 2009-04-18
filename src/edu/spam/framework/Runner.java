package edu.spam.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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
		
		// Sorry, commented out just so I can get straight to the Bayesian filter.
		//Class<?>[] filters = getClasses("edu.spam.framework.filters")
		
		 //for (Class<?> c : filters) {
			Class<?> c = edu.spam.framework.filters.bayesian.BayesianFilter.class;
			System.out.println(c);
			Filter filter = (Filter)c.newInstance();
			
			// Train
			Scanner trainInput = new Scanner(new File("trec06p/full/index"));
			int messageCount = 0;
			while (trainInput.hasNextLine() && messageCount < 20000) {
				++messageCount;
				String line = trainInput.nextLine();
				String[] parts = line.split(" ");
				File dataFile = new File("trec06p/full", parts[1]);
				MimeMessage message = MessageParser.parseMessage(dataFile.getAbsolutePath());
				filter.train(message, parts[0].equals("spam"));
			}
			
			// Test
			// Just to see if this works, we pick up where the trainer cut off due
			// to heap size limitations.
			int falsePositives = 0;
			int rightGuesses = 0;
			int totalGuesses = 0;
			while (trainInput.hasNextLine()) {
				String line = trainInput.nextLine();
				String[] parts = line.split(" ");
				File dataFile = new File("trec06p/full", parts[1]);
				MimeMessage message = MessageParser.parseMessage(dataFile.getAbsolutePath());
				++totalGuesses;
				try {
					float result = filter.test(message);
					// Right guesses
					if((parts[0].equals("spam") && result > 0) || 
						parts[0].equals("ham") && result == 0) {
						++rightGuesses;
					}
					if(parts[0].equals("ham") && result > 0) {
						++falsePositives;
					}
					System.out.println("Message was " + parts[0] + 
						", filter returned " + result + ". Accuracy: " + 
						(float)rightGuesses / (float)totalGuesses + 
						", false positives: " + (float)falsePositives / (float)totalGuesses);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			trainInput.close();
			
			// we should download trec07 for the test data  it'll be similar training method.
		//}
	}
	
	/**
	* list Classes inside a given package
	* @author Jon Peck http://jonpeck.com (adapted from http://www.javaworld.com/javaworld/javatips/jw-javatip113.html)
	* @param pckgname String name of a Package, EG "java.lang"
	* @return Class[] classes inside the root of the given package
	* @throws ClassNotFoundException if the Package is invalid
	*/
	public static Class<?>[] getClasses(String pckgname)
			throws ClassNotFoundException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
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
		Class<?>[] classesA = new Class[classes.size()];
		classes.toArray(classesA);
		return classesA;
	} 
}