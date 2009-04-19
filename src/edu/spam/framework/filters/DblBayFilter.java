package edu.spam.framework.filters;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * remove duplicate letters
 * @author qbproger
 *
 */
public class DblBayFilter extends BayesianFilter {
	
	protected String[] getTokens(MimeMessage msg) throws IOException, MessagingException {
		String[] words =  super.getTokens(msg);
		
		for (int k=0; k<words.length; k++) {

			StringBuilder builder = new StringBuilder();
			String w = words[k];
			
			builder.append(w.charAt(0));
			for (int i=1; i<w.length(); i++) {
				int j = i-1;
				if (w.charAt(i) != w.charAt(j)) {
					builder.append(w.charAt(i));
				}
			}
			
			words[k] = builder.toString();
			
		}
		
		return words;
	}
}
