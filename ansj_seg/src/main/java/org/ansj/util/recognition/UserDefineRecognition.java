package org.ansj.util.recognition;

import java.util.List;

import love.cq.domain.Forest;
import love.cq.domain.Value;
import love.cq.domain.WoodInterface;
import love.cq.library.Library;
import love.cq.util.ObjectBean;

import org.ansj.dic.DicReader;
import org.ansj.domain.Term;
import org.ansj.domain.TermNature;
import org.ansj.domain.TermNatures;
import org.ansj.util.TermUtil;

/**
 * 用户自定义词典.又称补充词典
 * 
 * TODO: 改为DoubleArray
 * 
 * @author ansj
 * 
 */
public class UserDefineRecognition {
	public static Forest FOREST = null;

	private Term[] terms = null;

	private WoodInterface branch = FOREST;

	private int offe = -1;
	private int endOffe = -1;
	private int tempFreq = 50;
	private String tempNature;

	public UserDefineRecognition(Term[] terms) {
		this.terms = terms;
	}

	public void recongnitionTerm() {
		if (FOREST == null) {
			return;
		}
		int length = terms.length - 1;

		for (int i = 0; i < length; i++) {
			if (terms[i] == null) continue;
			branch = termStatus(branch, terms[i]);
			if (branch == null) {
				if (offe != -1 && offe < endOffe) {
					i = offe;
					makeNewTerm();
				} else {
					if (offe != -1) {
						i = offe;
					}
					reset();
				}
			} else if (branch.getStatus() == 3) {
				endOffe = i;
				tempNature = branch.getParams()[0];
				tempFreq = ObjectBean.getInt(branch.getParams()[1], 50);
				if (offe != -1 && offe < endOffe) {
					i = offe;
					makeNewTerm();
				} else {
					reset();
				}
			} else if (branch.getStatus() == 2) {
				endOffe = i;
				if (offe == -1) {
					offe = i;
				} else {
					tempNature = branch.getParams()[0];
					tempFreq = ObjectBean.getInt(branch.getParams()[1], 50);
				}
			} else if (branch.getStatus() == 1) {
				if (offe == -1) {
					offe = i;
				}
			}
		}

		if (offe != -1 && offe < endOffe) {
			makeNewTerm();
		}

	}

	private void makeNewTerm() {
		StringBuilder sb = new StringBuilder();
		for (int j = offe; j <= endOffe; j++) {
			if (terms[j] == null) {
				continue;
			} else {
				sb.append(terms[j].getName());
			}
			terms[j] = null;
		}

		Term term = new Term(sb.toString(), offe, new TermNatures(new TermNature(tempNature, tempFreq)));
		TermUtil.insertTerm(terms, term);
		reset();
	}

	/**
	 * 重置
	 */
	private void reset() {
		offe = -1;
		endOffe = -1;
		tempFreq = 50;
		tempNature = null;
		branch = FOREST;
	}

	/**
	 * 传入一个term 返回这个term的状态
	 * 
	 * @param branch
	 * @param term
	 * @return
	 */
	private WoodInterface termStatus(WoodInterface branch, Term term) {
		String name = term.getName();
		for (int j = 0; j < name.length(); j++) {
			branch = branch.get(name.charAt(j));
			if (branch == null) {
				return null;
			}
		}
		return branch;
	}

	static {
		try {
			long start = System.currentTimeMillis();			
			FOREST = new Forest();
			List<String> lines = DicReader.getLines("buzzext.dic");

			for (String line: lines) {
				String[] strs = line.split("\t");
				Value value = new Value(strs[0], new String[]{"userDefine", strs[1]});
				Library.insertWord(FOREST, value);
			}
			System.out.println("加载buzzext.dic完成用时" + (System.currentTimeMillis() - start) + "毫秒");
		} catch (Exception e) {
			System.err.println("加载用户自定义词典buzzext.dic失败");
			e.printStackTrace();
		}
	}
}