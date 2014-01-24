package com.buzzinate.nlp.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphRank {
	private static double d = 0.75d;
	private Map<String, Integer> word2col = new HashMap<String, Integer>();
	private List<int[]> row2cols = new ArrayList<int[]>();
	
	public void addRow(List<String> words) {
//		System.out.println(row2cols.size() + " => " + words);
		int[] cols = new int[words.size()];
		for (int i = 0; i < words.size(); i++) {
			cols[i] = encode(words.get(i));
		}
		row2cols.add(cols);
	}
	
	public Map<String, Double> rank(List<String> words, List<String> ignoreWords) {
		double[] rowscore = new double[row2cols.size()];
		Arrays.fill(rowscore, 0d);
		double[] colscore = new double[word2col.size()];
		Arrays.fill(colscore, 0d);
		
		double avgScore = 1d / (1 + words.size());
		for (String word: words) {
			Integer col = word2col.get(word);
			if (col != null) {
				colscore[col] += avgScore;
			}
		}
		boolean[] cut = mincut(words, ignoreWords);
//		for (int i = 0; i < cut.length; i++) if (cut[i]) System.out.print(i + ",");
//		System.out.println(" <= " + words);
		
		// rs[c] = (1-d) * rs[c] + d * sum(g(r,c) * cs[c])
		for (int r = 0; r < rowscore.length; r++) rowscore[r] *= (1-d);
		for (int r = 0; r < rowscore.length; r++) {
			if (cut[r]) {
			  int[] cols = row2cols.get(r);
			  for (int col: cols) rowscore[r] += colscore[col] * d;
			} else rowscore[r] = 0;
		}
		
		// cs[c] = (1-d) * cs[c] + d * sum(g(r,c) * rs[r])
		for (int c = 0; c < colscore.length; c++) colscore[c] *= (1-d);
		for (int r = 0; r < rowscore.length; r++) {
			int[] cols = row2cols.get(r);
			for (int col: cols) colscore[col] += rowscore[r] * d;
		}
		
		// rs[c] = (1-d) * rs[c] + d * sum(g(r,c) * cs[c])
		for (int r = 0; r < rowscore.length; r++) rowscore[r] *= (1-d);
		for (int r = 0; r < rowscore.length; r++) {
			if (cut[r]) {
				int[] cols = row2cols.get(r);
				for (int col: cols) rowscore[r] += colscore[col] * d;
			} else rowscore[r] = 0;
		}

		// cs[c] = (1-d) * cs[c] + d * sum(g(r,c) * rs[r])
		for (int c = 0; c < colscore.length; c++) colscore[c] *= (1-d);
		for (int r = 0; r < rowscore.length; r++) {
			int[] cols = row2cols.get(r);
			for (int col: cols) colscore[col] += rowscore[r] * d;
		}
		
		HashMap<String, Double> wordscore = new HashMap<String, Double>();
		for (Map.Entry<String, Integer> e: word2col.entrySet()) {
			if (colscore[e.getValue()] > 0) wordscore.put(e.getKey(), colscore[e.getValue()]);
		}
		
//		System.out.println(wordscore);
		return wordscore;
	}
	
	private boolean[] mincut(List<String> words, List<String> ignoreWords) {
		int[] rowrank = new int[row2cols.size()];
		Arrays.fill(rowrank, -1);
		
		Set<Integer> thiswords = new HashSet<Integer>();
		for (String tw: words) {
			Integer idx = word2col.get(tw);
			if (idx != null) thiswords.add(idx);
		}
		Set<Integer> excludeWords = new HashSet<Integer>();
		for (String iw: ignoreWords) {
			Integer idx = word2col.get(iw);
			if (idx != null) excludeWords.add(idx);
		}
		
		int totalWords = 0;
		for (int[] cols: row2cols) totalWords += cols.length;
		
		int nWords = 0;
		int min = Integer.MAX_VALUE;
		int bestRank = -1;
		for (int k = 0; k < row2cols.size(); k++) {
			int max = 0;
			int mr = -1;
			for (int i = 0; i < rowrank.length; i++) {
				if (rowrank[i] < 0) {
					int cnt = 0;
					for (int c: row2cols.get(i)) {
						if (thiswords.contains(c) && !excludeWords.contains(c)) cnt++;
					}
					if (max < cnt) {
						max = cnt;
						mr = i;
					}
				}
			}
			if (max > 0) {
//				System.out.println(max + " :=> " + mr);
				rowrank[mr] = k;
				for (int c: row2cols.get(mr)) thiswords.add(c);
				nWords += row2cols.get(mr).length;
				if (nWords * 2> totalWords) break;
				if (min >= max) {
					min = max;
					bestRank = k;
				}
			} else break;
		}
		
		boolean[] cut = new boolean[rowrank.length];
		for (int i = 0; i < rowrank.length; i++) {
			if (rowrank[i] >= 0 && rowrank[i] <= bestRank) cut[i] = true;
			else cut[i] = false;
		}
		
		return cut;
	}
	
	private int encode(String word) {
		Integer col = word2col.get(word);
		if (col == null) {
			col = word2col.size();
			word2col.put(word, col);
		}
		return col;
	}

	public static void main(String[] args) {
		GraphRank g = new GraphRank();
		g.addRow(Arrays.asList("A", "B", "B", "C"));
		g.addRow(Arrays.asList("B", "C", "D"));
		g.addRow(Arrays.asList("A", "F"));
		g.addRow(Arrays.asList("F", "H", "G"));
		g.addRow(Arrays.asList("H", "G"));
		System.out.println(g.rank(Arrays.asList("B", "E"), Arrays.asList("F")));
	}
}