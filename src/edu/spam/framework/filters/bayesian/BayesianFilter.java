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
		float spamProbability = 0;
		
		BufferedReader messageReader = 
			new BufferedReader(new InputStreamReader(msg.getInputStream()));
		while(messageReader.ready()) {
			String line = messageReader.readLine();
			StringTokenizer messageLineTokenizer = new StringTokenizer(line);
			while(messageLineTokenizer.hasMoreElements()) {
				String word = (String)messageLineTokenizer.nextElement();
				spamProbability *= getProbabilityWordIndicatesSpam(word);
			}
		}
		return spamProbability;
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
							occurrence = this.wordOcurrences.put(word, new WordOcurrence(word));
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
		return wordOcurrences.get(word).getSpamOcurrences() / this.spamSampleCount;
	}
	
	private float getProbabilityWordAppearsInHam(String word) {
		return wordOcurrences.get(word).getHamOcurrences() / this.hamSampleCount;
	}
	
	private float getProbabilityWordIndicatesSpam(String word) {
		return (getProbabilityWordAppearsInSpam(word) * overallSpamProbability) /
			(getProbabilityWordAppearsInSpam(word) * overallSpamProbability + 
			getProbabilityWordAppearsInHam(word) * overallHamProbability);
	}

	private HashMap<String, WordOcurrence> wordOcurrences;
	private int spamSampleCount;
	private int hamSampleCount;
	
	// Wikipedia (duh we gotta cite a specific source) says .5 is "classical" but research indicates .8.
	// Not sure when the research was conducted or if it corresponds to our 2006 data.
	private final float overallSpamProbability = (float).5;
	private final  float overallHamProbability = (float)1 - overallSpamProbability;
}
