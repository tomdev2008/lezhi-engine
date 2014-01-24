package com.buzzinate.nlp.ngram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Suffix {
	private List<String> words;
	private int offset;
	
	public Suffix(List<String> words, int offset) {
		this.words = words;
		this.offset = offset;
	}
	
	public static List<Suffix> create(List<String> words) {
		List<Suffix> suffixes = new ArrayList<Suffix>();
		for (int i = 0; i < words.size(); i++) {
			suffixes.add(new Suffix(words, i));
		}
		return suffixes;
	}

	public List<String> getWords() {
		return words;
	}

	public int getOffset() {
		return offset;
	}
	
	public List<String> toWords() {
		return words.subList(offset, words.size());
	}

	public int compareTo(Suffix other, int maxLen) {
		int len = Math.min(words.size() - offset, other.words.size() - other.offset);
		int r = 0;
		for (int i = 0; i < len && i < maxLen; i++) {
			r = words.get(offset + i).compareTo(other.words.get(other.offset + i));
			if (r != 0) return r;
		}
		
		Integer size = words.size() - offset;
		return size.compareTo(other.words.size() - other.offset);
	}
	
	public static class SuffixCmp implements Comparator<Suffix> {
		private int maxLen;
		
		public SuffixCmp(int maxLen) {
			this.maxLen = maxLen;
		}

		@Override
		public int compare(Suffix s1, Suffix s2) {
			return s1.compareTo(s2, maxLen);			
		}
	}
}