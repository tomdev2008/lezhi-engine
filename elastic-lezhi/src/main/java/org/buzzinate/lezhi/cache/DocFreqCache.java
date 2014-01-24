package org.buzzinate.lezhi.cache;

public interface DocFreqCache {
	// get the docfreq for the term, return -1 if not found
	public int get(String term);
	// update cache
	public void put(String term, int docFreq);
}