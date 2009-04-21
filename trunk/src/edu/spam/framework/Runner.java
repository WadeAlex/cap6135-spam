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
import edu.spam.framework.filters.PhrBayFilter;
import edu.spam.framework.filters.SplChkBayFilter;

public class Runner {

	private static final float[] SPAM_THRESHOLDS = new float[]{0.4f, 0.45f, 0.5f, 0.55f, 0.6f, 0.65f, 0.7f, .75f, .8f/*, .85f*/};
	
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
		Class<?>[] filters = new Class<?>[]{PhrBayFilter.class, BayesianFilter.class, SplChkBayFilter.class, BayLiteFilter.class/*, DblBayFilter.class*/};
		
		//float[] percentToTrainWith = new float[]{.25f, .5f, .75f};
		float[] percentToTrainWith = new float[]{0.25f, .5f};
		
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
				
				// Train
				int trainCount = (int) ((float)totalMessages * percent);
				train(filter, trainCount);
				
				// Test				
				test(filter, trainCount, totalMessages);
				
				filter.clear();
			}
		}
	}
	
	public static void train(Filter f, int trainCount) throws FileNotFoundException {
		Scanner trainInput = new Scanner(new File("trec06p/full/index"));
		int messageCount = 0;
		while (trainInput.hasNextLine() && messageCount < trainCount) {
			++messageCount;
			String line = trainInput.nextLine();
			String[] parts = line.split(" ");
			File dataFile = new File("trec06p/full", parts[1]);
			MimeMessage message = MessageParser.parseMessage(dataFile.getAbsolutePath());
			f.train(message, parts[0].equalsIgnoreCase("spam"));
		}
		trainInput.close();
	}
	
	public static void test(Filter f, int trainCount, int totalMessages) throws MessagingException, FileNotFoundException {
		
		for (float spamThreshold : SPAM_THRESHOLDS) {
			int falsePositives = 0;
			int rightGuesses = 0;
			int totalGuesses = 0;
			
			Scanner trainInput = new Scanner(new File("trec06p/full/index"));
			int i=0;
			while (i < trainCount) {
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
					float result = f.test(message);
					// Right guesses
					if((parts[0].equalsIgnoreCase("spam") && result >= spamThreshold) || 
						(parts[0].equalsIgnoreCase("ham") && result < spamThreshold)) {
						++rightGuesses;
					} else if(parts[0].equalsIgnoreCase("ham") && result >= spamThreshold) {
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
			
			System.out.printf("Training: %-5d of %-5d Threshold: %f Accuracy: %4f False Positive %4f%n",
					trainCount, totalMessages, spamThreshold,
					(float)rightGuesses / (float)totalGuesses, (float)falsePositives / (float)totalGuesses);
		}
	}
}
