package com.buzzinate.nlp.segment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.splitWord.Segment;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.buzzinate.nlp.segment.Atom.AtomType;
import com.buzzinate.nlp.util.DictUtil;
import com.buzzinate.nlp.util.TextUtil;

public class AtomSplit {
	private static Set<String> stopwords = new HashSet<String>(Arrays.asList("ha", "further", "herself", "or", "itself", "becaus", "her", "again", "at", "then", "thei", "am", "few", "how", "can", "had", "that", "it", "let’", "and", "i’d", "from", "these", "he", "himself", "he’ll", "can’t", "he’", "do", "befor", "of", "into", "onc", "as", "an", "i’v", "our", "each", "but", "all", "not", "i’m", "most", "i", "below", "other", "him", "be", "how’", "on", "did", "i’ll", "no", "ar", "didn’t", "me", "nor", "ani", "here", "don’t", "couldn’t", "dure", "he’d", "down", "abov", "mustn’t", "onli", "cannot", "hi", "to", "thi", "ought", "hasn’t", "here’", "hadn’t", "doe", "against", "after", "if", "the", "aren’t", "in", "have", "their", "haven’t", "both", "by", "my", "such", "more", "between", "there", "about", "myself", "with", "it’", "been", "doesn’t", "for", "a", "wa", "is", "could", "isn’t", "off", "will"));
	private static Set<String> sentenceNatures = new HashSet<String>(Arrays.asList("null", "w"));
	
	public static void main(String[] args) throws IOException {
		System.out.println(splitSnippets("该名乘客,三星i9100真伪查询352110052278091帮助查一下这"));
		System.out.println(splitSnippets("线下体验，线上购物"));
		System.out.println(splitSnippets("著名的《清明上河图》，也和这位书画皇帝不无干系。张择端完成这幅歌颂太平盛世历史长卷后，首先呈献给宋徽宗，宋徽宗因此成为此画的第一位收藏者。作为中国历史上书画大家的宋徽宗酷爱此画，用“瘦金体”亲笔在图上题写了“清明上河图”五个字，并钤上了双龙小印。"));
		System.out.println(split("2011-34-43 为中国定制 英菲尼迪M35hL北京车展首发"));
//		System.out.println(splitSnippets("为中国定制 英菲尼迪M35hL北京车展首发"));
		System.out.println(splitSnippets("Dan Haywood's Domain-Driven Design of the Using Naked Objects"));
	}
	
	public static List<String> split0(String text) {
		List<String> result =  new ArrayList<String>();
		for (Atom atom: split(text)) result.add(atom.token);
		return result;
	}
	
	public static double atomLength(String text) {
		double length = 0;
		for (Atom atom: split(text)) {
			if (atom.atomType == AtomType.AT_LETTER && atom.token.length() > 2) length += 1.5;
			else length += 1;
		}
		return length;
	}
	
	public static int count(String text) {
		return split(text).size();
	}
	
	public static List<Atom> split(String text) {
		List<Atom> result = new ArrayList<Atom>();
		int last = 0;
		AtomType t = AtomType.AT_LETTER;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (TextUtil.isAlphaOrDigit(ch) || ch == '\'' || ch == '.') {
				if (i == last) {
					t = AtomType.AT_LETTER;
					if (Character.isDigit(ch)) t = AtomType.AT_NUM;
				}
			} else if (Character.isLetter(ch)) {
				if (i > last) result.add(new Atom(text.substring(last, i), t));
				result.add(new Atom(text.substring(i, i+1), AtomType.AT_CHINESE));
				last = i + 1;
			} else {
				if (i > last) result.add(new Atom(text.substring(last, i), t));
				if (t != AtomType.AT_LETTER || !isConnectChar(ch)) result.add(new Atom(text.substring(i, i+1), AtomType.AT_PUNC));
				last = i + 1;
			}
		}
		if (text.length() > last) result.add(new Atom(text.substring(last, text.length()), t));
		return result;
	}
	
	public static boolean isConnectChar(char ch) {
		return Character.isWhitespace(ch) || ch == '-';
	}
	
	public static List<String> splitSentences(String text) throws IOException {
		List<String> result = new ArrayList<String>();
		int last = 0;
		for (Term term: Segment.split(text, ToAnalysis.USE_USER_DEFINE)) {
			if (sentenceNatures.contains(term.getNatrue().natureStr) && !isWhiteSpace(term.getName())) {
				if (term.getOffe() > last) {
					String snippet = text.substring(last, term.getOffe()).trim();  
					if (snippet.length() > 0) result.add(snippet);
				}
				last = term.getOffe() + term.getName().length();
			}
		}
		if (text.length() > last) {
			String snippet = text.substring(last, text.length()).trim();  
			if (snippet.length() > 0) result.add(snippet);
		}
		return result;
	}
	
	public static List<String> splitSnippets(String text) throws IOException {
		List<String> result = new ArrayList<String>();
		int last = 0;
		for (Term term: Segment.split(text, ToAnalysis.USE_USER_DEFINE)) {
			if ((DictUtil.isUseless(term) || stopwords.contains(TextUtil.stem(term.getName()))) && !isWhiteSpace(term.getName())) {
				if (term.getOffe() > last) {
					String snippet = text.substring(last, term.getOffe()).trim();  
					if (snippet.length() > 0) result.add(snippet);
				}
				last = term.getOffe() + term.getName().length();
			}
		}
		if (text.length() > last) {
			String snippet = text.substring(last, text.length()).trim();  
			if (snippet.length() > 0) result.add(snippet);
		}
		return result;
	}
	
	private static boolean isWhiteSpace(String term) {
		return term.length() == 1 && (Character.isWhitespace(term.charAt(0)) || term.charAt(0) == '-');
	}
}