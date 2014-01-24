package com.buzzinate.nlp.segment;

import java.util.List;

import com.buzzinate.nlp.util.TextUtil;

public class Atom {
	public static enum AtomType { AT_CHINESE, AT_LETTER, AT_NUM, AT_PUNC }
	
	public String token;
	public AtomType atomType;
	
	public Atom(String token, AtomType atomType) {
		this.token = token;
		this.atomType = atomType;
	}
	
	@Override
	public String toString() {
		return token + "(" + atomType + ")";
	}
	
	public static String join(List<String> atoms) {
		StringBuffer sb = new StringBuffer();
		boolean prevEn = false;
		for (String atom: atoms) {
			if (TextUtil.isFirstAscii(atom)) {
				if (prevEn) sb.append(" ");
				prevEn = true;
			} else {
				prevEn = false;
			}
			sb.append(atom);
		}
		return sb.toString();
	}
	
	public static String join(String[] atoms, int start, int end) {
		StringBuffer sb = new StringBuffer();
		boolean prevEn = false;
		for (int i = start; i < end; i++) {
			String atom = atoms[i];
			if (TextUtil.isFirstAscii(atom)) {
				if (prevEn) sb.append(" ");
				prevEn = true;
			} else {
				prevEn = false;
			}
			sb.append(atom);
		}
		return sb.toString();
	}
}