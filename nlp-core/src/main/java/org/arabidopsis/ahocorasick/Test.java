package org.arabidopsis.ahocorasick;

import java.util.Arrays;
import java.util.Iterator;

public class Test {
	public static void main(String[] args) {
		 AhoCorasick<String> tree = new AhoCorasick<String>();
	       tree.add("中国".getBytes(), "中国");
	       tree.add("中国".getBytes(), "中国");
	       tree.add("站起".getBytes(), "站起");
	       tree.prepare();

	       String text = "从此中国人民站起来了";
	       int len = text.length();
	       int[] offset = new int[len];
	       int off = 0;
	       for (int i = 0; i < text.length(); i++) {
	    	   offset[i] = off;
	    	   byte[] cbs = text.substring(i, i+1).getBytes();
	    	   off += cbs.length;
	       }

	       Iterator<SearchResult<String>> searcher = tree.search("从此中国人民站起来了".getBytes());
	       while (searcher.hasNext()) {
	           SearchResult<String> result = searcher.next();
	           for (Object o: result.getOutputs())
	        	   System.out.print(o + ", ");
	           System.out.println();
	           System.out.println("Found at index: " + result.getLastIndex());
	           int idx = Arrays.binarySearch(offset, result.getLastIndex());
	           System.out.println(idx);
	       }
	}
}
