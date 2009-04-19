package edu.spam.framework.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.sun.mail.util.DecodingException;

/**
 * Takes only the first and last 10 tokens by rank. 
 * @author qbproger
 *
 */
public class BayLiteFilter extends BayesianFilter {

	@Override
	public float test(MimeMessage msg) throws MessagingException {
		// spamProbability is p1 * p2 * ... * p(n - 1) * pn,
		// inverseSpamProbability is (1-p1) * (1-p2) ... * p(1-n)
		// see Bayes' theorem for more details.
		double spamProbability = 1;
		double inverseSpamProbability = 1;
		
		ArrayList<Double> probs = new ArrayList<Double>();
		
		try {
			String[] words = getTokens(msg);
			for (String word : words) {
				double probability = getProbabilityWordIndicatesSpam(word);
				
				probs.add(probability);
			}
		} catch (DecodingException e) {
			return 1f;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Collections.sort(probs);
		for (int i=probs.size()-10; i>10; i--)
			probs.remove(i);
		
		for (Double d:probs) {
			spamProbability *= d;
			inverseSpamProbability *= (1.0 - d);
		}
		
		double output = spamProbability / (spamProbability + inverseSpamProbability);
		if (Double.isNaN(output))
			return .5f;
		return (float)output;
	}
}
