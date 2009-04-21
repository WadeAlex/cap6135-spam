package edu.spam.framework.filters;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class PhrBayFilter extends BayesianFilter {
	public String[] getTokens(MimeMessage msg) throws IOException, MessagingException {
		String[] output = super.getTokens(msg);
		
		if (output.length > 0) {
			String[] newOut = new String[output.length + output.length - 1];
			for (int i=0; i<newOut.length - 1; i += 2) {
				newOut[i] = output[i/2];
				newOut[i+1] = output[i/2] + " " + output[i/2+1];
			}
			
			newOut[newOut.length-1] = output[output.length-1];
			
			return newOut;
		}
		
		return output;
	}
}
