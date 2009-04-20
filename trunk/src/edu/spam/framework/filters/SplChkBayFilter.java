package edu.spam.framework.filters;

/**
 * Run a simple spell checking pass on the words.  This actually marginally
 * increases accuracy. (runs slow)
 * @author qbproger
 *
 */
public class SplChkBayFilter extends BayesianFilter {

	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	
	protected double getProbabilityWordIndicatesSpam(String word) {
		int[] ocurrence = this.wordOcurrences.get(word);
		if (ocurrence == null) {
			//System.out.println(word);
			word = checkSpelling(word);
			if (word != null) {
				ocurrence = this.wordOcurrences.get(word);
			}
			
			if (ocurrence == null)
				return 0.6;
			
		}
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
	
	private String checkSpelling(String word) {
		String output = word.replaceAll("[^\\p{ASCII}]", "");
		if (!output.equals(word)) {
			return null;
		}

		int n = word.length();
		// deletion
		for (int i = 0; i < n; i++) {
			String str = word.substring(0, i) + word.substring(i + 1);
			if (this.wordOcurrences.containsKey(str)) {
				return str;
			}
			
			if (i < n-1) {
				str = word.substring(0, i) + word.charAt(i + 1) + word.charAt(i) + word.substring(i + 2);
				if (this.wordOcurrences.containsKey(str)) {
					return str;
				}
			}
			
			for (int j = 0; j < ALPHABET.length(); j++) {
				// alteration
				char c = ALPHABET.charAt(j);
				str = word.substring(0, i) + c + word.substring(i + 1);
				if (this.wordOcurrences.containsKey(str)) {
					return str;
				}

				// insertion
				str = word.substring(0, i) + c + word.substring(i);
				if (this.wordOcurrences.containsKey(str)) {
					return str;
				}
			}
		}
		
		for (int j = 0; j < ALPHABET.length(); j++) {
			String str = word.substring(0, n) + ALPHABET.charAt(j) + word.substring(n);
			if (this.wordOcurrences.containsKey(str)) {
				return str;
			}
		}
		
		return null;
	}

}
