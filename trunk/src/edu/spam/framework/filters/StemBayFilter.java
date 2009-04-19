package edu.spam.framework.filters;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

/**
 * run a stemmer on the tokens
 * @author qbproger
 *
 */
public class StemBayFilter extends BayesianFilter {

	protected String[] getTokens(MimeMessage msg) throws IOException, MessagingException {
		String[] words = super.getTokens(msg);
		
		SnowballStemmer stemmer = new englishStemmer(); 
		
		for(int i=0; i<words.length; i++) {
			stemmer.setCurrent(words[i]);
			stemmer.stem();
			words[i] = stemmer.getCurrent();
		}
		
		return words;
	}
	
}
