package edu.spam.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import edu.spam.framework.filters.BayLiteFilter;
import edu.spam.framework.filters.BayesianFilter;
import edu.spam.framework.filters.SplChkBayFilter;

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
		Class<?>[] filters = new Class<?>[]{BayesianFilter.class, SplChkBayFilter.class, BayLiteFilter.class/*, DblBayFilter.class*/};
		
		//float[] percentToTrainWith = new float[]{.25f, .5f, .75f};
		float[] percentToTrainWith = new float[]{0.25f, .5f};
		float[] spamThresholds = new float[]{.75f, .8f/*, .85f*/};
		
		int totalMessages = 0;
		Scanner trainInput = new Scanner(new File("trec06p/full/index"));
		while (trainInput.hasNextLine()) {
			trainInput.nextLine();
			totalMessages++;
		}
		trainInput.close();
		
		for (Class<?> c : filters) {

			System.out.println(c.getSimpleName());
			for (float percent : percentToTrainWith) {
				Filter filter = (Filter)c.newInstance();
				
				int trainCount = (int) ((float)totalMessages * percent);
				// Train
				trainInput = new Scanner(new File("trec06p/full/index"));
				int messageCount = 0;
				while (trainInput.hasNextLine() && messageCount < trainCount) {
					++messageCount;
					String line = trainInput.nextLine();
					String[] parts = line.split(" ");
					File dataFile = new File("trec06p/full", parts[1]);
					MimeMessage message = MessageParser.parseMessage(dataFile.getAbsolutePath());
					filter.train(message, parts[0].equals("spam"));
				}
				trainInput.close();
				
				// Test
				// Just to see if this works, we pick up where the trainer cut off due
				// to heap size limitations.
				
				for (float spamThreshold : spamThresholds) {
					int falsePositives = 0;
					int rightGuesses = 0;
					int totalGuesses = 0;
					
					trainInput = new Scanner(new File("trec06p/full/index"));
					int i=0;
					while (i<trainCount) {
						trainInput.nextLine();
						i++;
					}
					
					while (trainInput.hasNextLine()) {
						String line = trainInput.nextLine();
						String[] parts = line.split(" ");
						File dataFile = new File("trec06p/full", parts[1]);
						MimeMessage message = MessageParser.parseMessage(dataFile.getAbsolutePath());
						++totalGuesses;
						try {
							float result = filter.test(message);
							// Right guesses
							if((parts[0].equals("spam") && result >= spamThreshold) || 
								(parts[0].equals("ham") && result < spamThreshold)) {
								++rightGuesses;
							} else if(parts[0].equals("ham") && result >= spamThreshold) {
								++falsePositives;
							}
							
							/*if (totalGuesses % 500 == 0) 
							{
								System.out.printf("Input %4s Probability: %4f Accuracy: %4f False Positive %4f%n", 
										parts[0], result, (float)rightGuesses / (float)totalGuesses, (float)falsePositives / (float)totalGuesses);
							}*/
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				
					trainInput.close();
					
					System.out.printf("Training: %d of %d Threshold: %f Accuracy: %4f False Positive %4f%n",
							trainCount, totalMessages, spamThreshold,
							(float)rightGuesses / (float)totalGuesses, (float)falsePositives / (float)totalGuesses);
				}
				filter.clear();
			}
		}
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
