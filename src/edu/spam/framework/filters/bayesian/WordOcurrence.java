/**
 * 
 */
package edu.spam.framework.filters.bayesian;

public class WordOcurrence {
	public WordOcurrence(String word) {
		this.word = word;
	}
	
	private final String word;
	/**
	 * @return the spamOcurrences
	 */
	public int getSpamOcurrences() {
		return spamOcurrences;
	}
	/**
	 * @param spamOcurrences the spamOcurrences to set
	 */
	public void incrementSpamOcurrences() {
		++this.spamOcurrences;
	}
	/**
	 * @return the hamOcurrences
	 */
	public int getHamOcurrences() {
		return hamOcurrences;
	}
	/**
	 * @param hamOcurrences the hamOcurrences to set
	 */
	public void incrementHamOcurrences() {
		++this.hamOcurrences;
	}
	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}

	private int spamOcurrences;
	private int hamOcurrences;
}
