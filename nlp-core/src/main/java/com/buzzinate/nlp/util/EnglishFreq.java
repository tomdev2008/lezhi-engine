package com.buzzinate.nlp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.buzzinate.doublearray.DoubleArray;

public class EnglishFreq {
	private DoubleArray da = null;
	private int[] freqs = null;
	private int totalFreq = 22164985;
	
	public EnglishFreq() {
		try {
			da = new DoubleArray(Thread.currentThread().getContextClassLoader().getResourceAsStream("english.freq.array"));
			freqs = new int[da.size()];

			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("english.freq");
			List<String> lines = IOUtils.readLines(is, "UTF-8");
			IOUtils.closeQuietly(is);

			for (String line : lines) {
				String[] strs = line.split("\t");
				String word = strs[0];
				int freq = Integer.parseInt(strs[1]);
				int wid = da.getWordIdx(word);
				freqs[wid] = freq;
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize english dict", e);
		}
	}
	
	public int totalFreq() {
		return totalFreq;
	}
	
	public int freq(String word) {
		int wid = da.getWordIdx(word);
		if (wid >= 0) return freqs[wid];
		return 0;
	}

	public static void main(String[] args) throws IOException {
		EnglishFreq ef = new EnglishFreq();
		System.out.println(ef.freq("the"));
	}
}