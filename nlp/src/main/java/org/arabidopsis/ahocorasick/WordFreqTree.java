package org.arabidopsis.ahocorasick;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.buzzinate.nlp.util.TextUtil;

public class WordFreqTree {
	private static final Charset utf8 = Charset.forName("UTF-8");
	private AhoCorasick<String> tree;
	
	public WordFreqTree() {
		this.tree = new AhoCorasick<String>();
	}
	
	public void add(String word) {
		tree.add(TextUtil.fillWord(word).getBytes(utf8), word);
	}
	
	public void build() {
		tree.prepare();
	}
	
	public String searchFirst(String text) {
		text = TextUtil.fillText(text);
		Iterator<SearchResult<String>> s = tree.search(text.getBytes(utf8));
		if (s.hasNext()) {
			SearchResult<String> sr = s.next();
			Set<String> outputs = sr.getOutputs();
			if (outputs.size() > 0) return outputs.toArray(new String[0])[0];
		}
		return null;
	}
	
	public String searchMax(String text) {
		text = TextUtil.fillText(text);
		Iterator<SearchResult<String>> s = tree.search(text.getBytes(utf8));
		String r = null;
		int maxLen = 0;
		while (s.hasNext()) {
			SearchResult<String> sr = s.next();
			Set<String> words = sr.getOutputs();
			for (String word:words) {
				if (word.length() > maxLen) {
					maxLen = word.length();
					r = word;
				}
			}
		}
		return r;
	}
	
	
	
	public List<String> search(String text) {
		text = TextUtil.fillText(text);
		List<String> words = new ArrayList<String>();
		Iterator<SearchResult<String>> s = tree.search(text.getBytes(utf8));
		while (s.hasNext()) {
			SearchResult<String> sr = s.next();
			words.addAll(sr.getOutputs());
		}
		
		return words;
	}
	
	public static void main(String[] args) {
		WordFreqTree wft = new WordFreqTree();
		wft.add("c罗");
		wft.build();
		List<String> ws = wft.search("C罗加盟仪式用掉西媒3小时 球迷实际人数超9万");
		System.out.println(ws);
	}
}
