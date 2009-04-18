/**
 * 
 */
package edu.spam.framework.filters.bayesian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import edu.spam.framework.Filter;

public class BayesianFilter implements Filter {

	public final int HAM = 0;
	public final int SPAM = 1;

	Pattern TOKEN_PATTERN = Pattern.compile("\\$?\\d*(?:[.,]\\d+)+|\\w+-\\w+|\\w+");
	
	/* (non-Javadoc)
	 * @see edu.spam.framework.Filter#test(javax.mail.internet.MimeMessage)
	 */
	@Override
	public float test(MimeMessage msg) throws IOException, MessagingException {
		// spamProbability is p1 * p2 * ... * p(n - 1) * pn,
		// inverseSpamProbability is (1-p1) * (1-p2) ... * p(1-n)
		// see Bayes' theorem for more details.
		double spamProbability = 1;
		double inverseSpamProbability = 1;
		
		String[] words = getTokens(msg);
		for (String word : words) {
			double probability = getProbabilityWordIndicatesSpam(word);
			
			spamProbability *= probability;
			inverseSpamProbability *= (1 - probability);
		}
		
		double output = spamProbability / (spamProbability + inverseSpamProbability);
		if (Double.isNaN(output))
			return .5f;
		return (float)output;
	}

	/* (non-Javadoc)
	 * @see edu.spam.framework.Filter#train(javax.mail.internet.MimeMessage)
	 */
	@Override
	public boolean train(MimeMessage msg, boolean spam) {
		try {
			if(spam) {
				++this.spamSampleCount;
			} else {
				++this.hamSampleCount;
			}
			
			
			String[] words = getTokens(msg);
			
					
			for(String word : words) {
				int[] occurrence = this.wordOcurrences.get(word);
				
				if(occurrence == null) {
					occurrence = new int[2];
					this.wordOcurrences.put(word, occurrence);
				}
				
				if(spam) {
					occurrence[SPAM]++;
				} else {
					occurrence[HAM]++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	protected String[] getTokens(MimeMessage msg) throws IOException, MessagingException {
		// This makes sure we don't make multiple entries for the same word per message.
		// Based on my interpretation, the basic Bayesian filter only counts the first
		// ocurrence.
		HashSet<String> encounteredWords = new HashSet<String>();
		
		BufferedReader messageReader = 
			new BufferedReader(new InputStreamReader(msg.getInputStream()));
		
		while(messageReader.ready()) {
			String line = messageReader.readLine();
			Matcher matcher = TOKEN_PATTERN.matcher(line);
			
			while(matcher.find()) {
				
				String word = matcher.group().toLowerCase();
				if (word.length() < 2)
					continue;
				
				if(!encounteredWords.contains(word)) {
					encounteredWords.add(word);
				}
			}
		}
		messageReader.close();
		
		return encounteredWords.toArray(new String[encounteredWords.size()]);
	}
	
	private double getProbabilityWordIndicatesSpam(String word) {
		int[] ocurrence = this.wordOcurrences.get(word);
		if (ocurrence == null)
			return 0.4;
		if (ocurrence[SPAM] > 0 && ocurrence[HAM] == 0) {
			return 0.99;
		} else if (ocurrence[SPAM] == 0 && ocurrence[HAM] > 0) {
			return 0.01;
		} else if (ocurrence[SPAM] > 0 && ocurrence[HAM] > 0) {
			double ham = (double)ocurrence[HAM] / (double)hamSampleCount;
			double spam = (double)ocurrence[SPAM] / (double)spamSampleCount;
			double rating = spam / (ham+spam);
			if (rating < 0.01)
				return 0.01;
			if (rating > 0.99)
				return 0.99;
			return rating;
		}
		return 0.4;
	}

	private HashMap<String, int[]> wordOcurrences = new HashMap<String, int[]>();
	private int spamSampleCount;
	private int hamSampleCount;
	
	// Wikipedia (duh we gotta cite a specific source) says .5 is "classical" but research indicates .8.
	// Not sure when the research was conducted or if it corresponds to our 2006 data.
	private final float overallSpamProbability = (float).5;
	private final  float overallHamProbability = (float)1 - overallSpamProbability;
}
