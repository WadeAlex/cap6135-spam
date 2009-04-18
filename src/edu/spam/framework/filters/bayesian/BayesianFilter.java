/**
 * 
 */
package edu.spam.framework.filters.bayesian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import edu.spam.framework.Filter;

public class BayesianFilter implements Filter {

	/* (non-Javadoc)
	 * @see edu.spam.framework.Filter#test(javax.mail.internet.MimeMessage)
	 */
	@Override
	public float test(MimeMessage msg) throws IOException, MessagingException {
		// spamProbability is p1 * p2 * ... * p(n - 1) * pn,
		// inverseSpamProbability is (1-p1) * (1-p2) ... * p(1-n)
		// see Bayes' theorem for more details.
		float spamProbability = 1;
		float inverseSpamProbability = 1;
		
		BufferedReader messageReader = 
			new BufferedReader(new InputStreamReader(msg.getInputStream()));
		while(messageReader.ready()) {
			String line = messageReader.readLine();
			StringTokenizer messageLineTokenizer = new StringTokenizer(line);
			while(messageLineTokenizer.hasMoreElements()) {
				String word = (String)messageLineTokenizer.nextElement();
				float probability = getProbabilityWordIndicatesSpam(word);
				if(probability != 0 && !Float.isNaN(probability)) {
					spamProbability *= probability;
				}
				if(spamProbability != 1 && !Float.isNaN(probability)) {
					inverseSpamProbability *= (1 - spamProbability);
				}
			}
		}
		
		return spamProbability / (spamProbability + inverseSpamProbability);
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
			
			// This makes sure we don't make multiple entries for the same word per message.
			// Based on my interpretation, the basic Bayesian filter only counts the first
			// ocurrence.
			HashSet<String> encounteredWords = new HashSet<String>();
			
			BufferedReader messageReader = 
				new BufferedReader(new InputStreamReader(msg.getInputStream()));
			while(messageReader.ready()) {
				String line = messageReader.readLine();
				StringTokenizer messageLineTokenizer = new StringTokenizer(line);
				while(messageLineTokenizer.hasMoreElements()) {
					String word = (String)messageLineTokenizer.nextElement();
					if(!encounteredWords.contains(word)) {
						encounteredWords.add(word);
						WordOcurrence occurrence = this.wordOcurrences.get(word);
						if(occurrence == null) {
							occurrence = new WordOcurrence(word);
							this.wordOcurrences.put(word, occurrence);
						}
						if(spam) {
							occurrence.incrementSpamOcurrences();
						} else {
							occurrence.incrementHamOcurrences();
						}
					}
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
	
	private float getProbabilityWordAppearsInSpam(String word) {
		WordOcurrence ocurrence = this.wordOcurrences.get(word);
		if(ocurrence == null) {
			return 0;
		}
		return (float)ocurrence.getSpamOcurrences() / (float)this.spamSampleCount;
	}
	
	private float getProbabilityWordAppearsInHam(String word) {
		WordOcurrence ocurrence = this.wordOcurrences.get(word);
		if(ocurrence == null) {
			return 0;
		}
		return (float)ocurrence.getHamOcurrences() / (float)this.hamSampleCount;
	}
	
	private float getProbabilityWordIndicatesSpam(String word) {
		return (getProbabilityWordAppearsInSpam(word) * overallSpamProbability) /
			(getProbabilityWordAppearsInSpam(word) * overallSpamProbability + 
			getProbabilityWordAppearsInHam(word) * overallHamProbability);
	}

	private HashMap<String, WordOcurrence> wordOcurrences = new HashMap<String, WordOcurrence>();
	private int spamSampleCount;
	private int hamSampleCount;
	
	// Wikipedia (duh we gotta cite a specific source) says .5 is "classical" but research indicates .8.
	// Not sure when the research was conducted or if it corresponds to our 2006 data.
	private final float overallSpamProbability = (float).5;
	private final  float overallHamProbability = (float)1 - overallSpamProbability;
}
