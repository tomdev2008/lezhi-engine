package com.buzzinate.nlp.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.library.InitDictionary;
import org.ansj.library.TwoWordLibrary;
import org.ansj.splitWord.Segment;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang.StringUtils;

public class DictUtil {
	private static final Set<String> uselessNatures = new HashSet<String>(Arrays.asList("null", "w", "mq", "r", "d", "p", "y", "ad", "c", "u", "ud", "ug", "uj", "ul", "uv", "uz", "l"));
	private static final Set<String> specialChars = new HashSet<String>(Arrays.asList("是", "日", "在", "使", "让", "称之为", "意味着", "有利于", "ha", "further", "herself", "or", "itself", "becaus", "her", "again", "at", "then", "thei", "am", "few", "how", "can", "had", "that", "it", "let’", "and", "i’d", "from", "these", "he", "himself", "he’ll", "can’t", "he’", "do", "befor", "of", "into", "onc", "as", "an", "i’v", "our", "each", "but", "all", "not", "i’m", "most", "i", "below", "other", "him", "be", "how’", "on", "did", "i’ll", "no", "ar", "didn’t", "me", "nor", "ani", "here", "don’t", "couldn’t", "dure", "he’d", "down", "abov", "mustn’t", "onli", "cannot", "hi", "to", "thi", "ought", "hasn’t", "here’", "hadn’t", "doe", "against", "after", "if", "the", "aren’t", "in", "have", "their", "haven’t", "both", "by", "my", "such", "more", "between", "there", "about", "myself", "with", "it’", "been", "doesn’t", "for", "a", "wa", "is", "could", "isn’t", "off", "will", "also"));
	
	private static final int CN_MAX_FREQ = InitDictionary.termNatures[InitDictionary.getWordId("，")].allFreq * 2 + 1;
	private static final double maxCnIdf = Math.log(CN_MAX_FREQ);
	
	private static final EnglishFreq engDict = new EnglishFreq();
	private static final double maxEnIdf = Math.log(engDict.totalFreq() + 1);
	
	public static boolean isUseless(Term term) {
		if (specialChars.contains(term.getName())) return true;
		if (term.getNatrue().natureStr.equals("t")) {
			return !term.getName().contains("节");
		}
		if (term.getNatrue().natureStr.equals("f")) {
			return term.getFrom() != null && term.getFrom().getName().length() >= 2 || term.getName().length() > 1;
		}
		if (term.getNatrue().natureStr.equals("v")) {
			return term.getName().endsWith("于") || term.getName().endsWith("以");
		}
		if (term.getNatrue().natureStr.equals("q")) {
			return term.getFrom() != null && term.getFrom().getNatrue().natureStr.equals("r");
		}
		return uselessNatures.contains(term.getNatrue().natureStr);
	}
	
	public static double idf(String word) {
		if (TextUtil.isFirstAscii(word)) {
			int freq = engDict.freq(word);
			if (freq == 0) return 1;
			else return Math.log(engDict.totalFreq() / (1d + freq)) /maxEnIdf;
		} else if (TextUtil.isFirstDigit(word)) {
			if (word.equals("360")) return 1;
			else return 0.1;
		} else {
			int freq = Segment.freq(word);
			if (freq == 0) return 1;
			else return Math.log(CN_MAX_FREQ / (1d + freq)) / maxCnIdf;
		}
	}
	
	  // calculate the idf from ansj dictionary
	public static double splitIdf(String word) throws IOException {
		if (TextUtil.countNumWords(word) == 0) return 0;
		List<Term> terms = Segment.split(word, ToAnalysis.RECOGNTION_PERSION);
		int size = terms.size() + 2;
		Term lastTerm = terms.get(terms.size() - 1);
		double totalIdf = idf(lastTerm.getName()) * 2;
		for (Term term: terms) {
			if (StringUtils.isBlank(term.getName().trim())) continue;
			totalIdf += idf(term.getName());
		}
		if (terms.size() == 1) {
			String nature = terms.get(0).getNatrue().natureStr;
			double weight = 0.2d;
			if (nature.startsWith("n") && nature.length() > 1) weight = 1d;
			if (nature.equals("n") || nature.equals("en")) weight = 0.8d;
			if (nature.startsWith("a")) weight = 0.5d;
			totalIdf *= weight;
		}
		return totalIdf / size;
	}
	
	public static boolean isStop(String word) {
		return WordUtil.isStopword(word);
	}
	
	public static final void main(String[] args) throws IOException {
		System.out.println(CN_MAX_FREQ);
		for (String word: Arrays.asList("appl", "from", "hadoop", "中国", "或者", "火箭炮", "超过", "发现")) {
			System.out.println(word + " => idf: " + idf(word));
		}
		
		for (String word: Arrays.asList("---", "代步", "万实用大", "导购", "车内空气", "duratec", "福特嘉年华", "车导购", "206", "轴距", "感觉", "发动机", "hhrb", "行车感觉", "1.4", "标致207", "保时捷", "工薪", "空间利用率", "尺寸", "标致", "变速器", "三厢", "空间", "207", "家庭", "出色", "达到", "大规格")) {
			System.out.println(word + " => splitidf: " + splitIdf(word));
		}
		
		for (String word: Arrays.asList("李亚鹏", "认出", "英国", "伦敦", "英伦敦", "在伦敦", "纯法系", "小s", "谢霆锋", "过程中", "上市", "苹果", "的信息", "超过", "发现", "appl", "we also", "apach hadoop")) {
			System.out.println(word + " => splitidf: " + splitIdf(word));
		}
	
		String text = "乔布斯是一个聪明的人,你我之间，一名乘客，3名旅客";
//		String text = "线上体验，线下购物";
//		String text = "他的书法被世人称之为瘦金体";
//		String text = "十八大代表中出现许多“草根代表”";
//		String text = "过程中的Hadoop 0.89辆";
//		String text = "盘点明星怪癖秘闻 张柏芝Hadoop祼睡罗志祥坐着尿尿(10)_娱乐频道_光明网";
		Term prev = null;
		for (Term t: Segment.split(text)) {
			System.out.println(t + ", offset=" + t.getOffe() +", useless=" + isUseless(t));
			if (prev != null) {
				int freq = TwoWordLibrary.getTwoWordFreq(prev, t);
				System.out.println(freq);
			}
			prev = t;
		}
	}
}