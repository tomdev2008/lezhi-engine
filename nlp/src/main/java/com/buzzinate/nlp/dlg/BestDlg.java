package com.buzzinate.nlp.dlg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.Segment;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.buzzinate.nlp.segment.Atom;
import com.buzzinate.nlp.segment.AtomSplit;

public class BestDlg {
	public static interface Dict {
		double dlg(List<String> tokens);
	}
	
	public static List<String> splitDlg(Dict vc, List<String> text, int maxLen) {
		double[] os = new double[text.size() + 1];
		int[] prev = new int[text.size() + 1];
		os[0] = 0;
		prev[0] = -1;
		for (int k = 1; k <= text.size(); k++) {
			prev[k] = k - 1;
			os[k] = os[k - 1];
			for (int j = k - 2; j >= 0 && j >= k - maxLen - 1; j--) {
				double dlgjk = vc.dlg(text.subList(j, k));
				if (os[k] < os[j] + dlgjk) {
					os[k] = os[j] + dlgjk;
					prev[k] = j;
				}
			}
		}
		List<String> result = new ArrayList<String>();
		int p = text.size();
		while (prev[p] >= 0) {
			String sub = Atom.join(text.subList(prev[p], p));
			result.add(sub);
			p = prev[p];
		}
		Collections.reverse(result);
		return result;
	}
	
	public static List<String> splitDlg(Dict vc, String text, int maxLen) throws IOException {
		List<String> atoms = new ArrayList<String>();
		HashSet<Integer> idxes = new HashSet<Integer>();
		idxes.add(0);
		for (Term term: Segment.splitRaw(text, ToAnalysis.USE_USER_DEFINE)) {
			atoms.addAll(AtomSplit.split0(term.getName()));
			idxes.add(atoms.size());
		}
		double[] os = new double[atoms.size() + 1];
		int[] prev = new int[atoms.size() + 1];
		os[0] = 0;
		prev[0] = -1;
		for (int k = 1; k <= atoms.size(); k++) {
			prev[k] = k - 1;
			os[k] = os[k - 1];
			if (!idxes.contains(k)) continue;
			for (int j = k - 2; j >= 0 && j >= k - maxLen - 1; j--) {
				if (!idxes.contains(j)) continue;
				double dlgjk = vc.dlg(atoms.subList(j, k));
				if (os[k] < os[j] + dlgjk) {
					os[k] = os[j] + dlgjk;
					prev[k] = j;
				}
			}
		}
		List<String> result = new ArrayList<String>();
		int p = atoms.size();
		while (prev[p] >= 0) {
			String sub = Atom.join(atoms.subList(prev[p], p));
			result.add(sub);
			p = prev[p];
		}
		Collections.reverse(result);
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		final HashMap<String, Double> ngram2dlg = new HashMap<String, Double>();
		ngram2dlg.put("搜索", 5.1292353665051);
		ngram2dlg.put("搜索结", 7.000750584113525);
		ngram2dlg.put("搜索结果", 11.434074298219457);
		Dict dict = new Dict() {
			public double dlg(List<String> tokens) {
				Double dlg = ngram2dlg.get(Atom.join(tokens));
				if (dlg == null) dlg = 0d;
				return dlg;
			}
		};
		System.out.println(splitDlg(dict, "搜索结论", 5));
	}
}