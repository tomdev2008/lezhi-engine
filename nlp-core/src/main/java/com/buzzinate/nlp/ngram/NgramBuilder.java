package com.buzzinate.nlp.ngram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buzzinate.nlp.segment.Atom;
import com.google.common.collect.Lists;

public class NgramBuilder {
	public static Map<String, Integer> build(List<List<String>> snippets, int maxLen) {
		List<Suffix> suffixes = new ArrayList<Suffix>();
		for (List<String> snippet: snippets) {
			suffixes.addAll(Suffix.create(snippet));
		}
		Collections.sort(suffixes, new Suffix.SuffixCmp(maxLen + 1));
		Map<List<String>, Integer> normal = buildFromSuffix(suffixes, maxLen);
		
		suffixes = new ArrayList<Suffix>();
		for (List<String> snippet: snippets) {
			suffixes.addAll(Suffix.create(Lists.reverse(snippet)));
		}
		Collections.sort(suffixes, new Suffix.SuffixCmp(maxLen + 1));
		Map<List<String>, Integer> reverse = buildFromSuffix(suffixes, maxLen);
		
		HashMap<String, Integer> ngram2freq = new HashMap<String, Integer>();
		for (Map.Entry<List<String>, Integer> e: normal.entrySet()) {
			List<String> r = Lists.reverse(e.getKey());
			if (reverse.containsKey(r)) ngram2freq.put(Atom.join(e.getKey()), e.getValue());
		}
		return ngram2freq;
	}

	private static Map<List<String>, Integer> buildFromSuffix(List<Suffix> suffixes, int maxLen) {
		List<PhaseIdx> pis = new ArrayList<PhaseIdx>();
		
		int[] freq = new int[maxLen];
		Arrays.fill(freq, 0);
		String[] ft = new String[maxLen];
		Arrays.fill(ft, "");
		for (int k = 0; k < suffixes.size(); k++) {
			Suffix suffix = suffixes.get(k);
			List<String> terms = suffix.toWords();
			int i = 0;
			for (; i < freq.length && i < terms.size(); i++) {
				if (terms.get(i).equals(ft[i])) freq[i]++;
				else break;
			}
			for (;i < freq.length && i < terms.size(); i++) {
				if (freq[i] >= 2) {
					if (i > 0 || ft[i].length() > 1) pis.add(new PhaseIdx(i+1, freq[i], k-1));
				}
				freq[i] = 1;
			}
			for (;i < freq.length; i++) {
				if (freq[i] >= 2) {
					if (i > 0 || ft[i].length() > 1) pis.add(new PhaseIdx(i+1, freq[i], k-1));
				}
				freq[i] = 0;
			}
			
			i = 0;
			for (; i < freq.length && i < terms.size(); i++) {
				ft[i] = terms.get(i);
			}
			for (; i < freq.length; i++) {
				ft[i] = "";
			}
		}
		
		for (int i = 0;i < freq.length; i++) {
			if (freq[i] >= 2) {
				if (i > 0 || ft[i].length() > 1) pis.add(new PhaseIdx(i+1, freq[i], suffixes.size()-1));
			}
			freq[i] = 0;
		}
		
		Map<List<String>, Integer> ngram2freq = new HashMap<List<String>, Integer>();
		for (PhaseIdx pi: pis) {
			int start = pi.idx - pi.freq + 1;
			List<Integer> nextFreqs = new ArrayList<Integer>();
			String prev = "";
			int nextFreq = 0;
			for (int k = start;  k <= pi.idx; k++) {
				List<String> terms = suffixes.get(k).toWords();
				String next = "$";
				if (pi.len < terms.size()) next = terms.get(pi.len);
				if (prev.equals(next)) nextFreq++;
				else {
					if (nextFreq > 0) {
						if (prev.equals("$")) {
							for (int i = 0; i < nextFreq; i++) nextFreqs.add(1);
						} else nextFreqs.add(nextFreq);
					}
					nextFreq = 1;
				}
				prev = next;
			}
			if (nextFreq > 0) {
				if (prev.equals("$")) {
					for (int i = 0; i < nextFreq; i++) nextFreqs.add(1);
				} else nextFreqs.add(nextFreq);
			}
			double entropy = 0;
			double f = pi.freq;
			for (int nf: nextFreqs) entropy += - nf * Math.log(nf / f);
			entropy = entropy / f;
			List<String> words = suffixes.get(pi.idx).toWords().subList(0, pi.len);
//			if (Atom.join(words).contains("十八大") || Atom.join(Lists.reverse(words)).contains("十八大")) {
//				System.out.println(words + " ==> freq=" + pi.freq + ", entropy=" + entropy);
//			}
			if (entropy > 0d) {
				ngram2freq.put(words, pi.freq);
			}
		}
		
		return ngram2freq;
	}
}

class PhaseIdx {
	int len;
	int freq;
	int idx;
	
	public PhaseIdx(int len, int freq, int idx) {
		this.len = len;
		this.freq = freq;
		this.idx = idx;
	}
}