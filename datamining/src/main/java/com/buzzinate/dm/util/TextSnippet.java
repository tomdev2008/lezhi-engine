package com.buzzinate.dm.util;

import java.util.ArrayList;
import java.util.List;

public class TextSnippet {
	public static List<String> split(String text) {
		List<String> words = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (isAscii(ch) || Character.isDigit(ch)) {
				if (sb.length() > 0) words.add(sb.toString());
				sb.setLength(0);
			} else if (Character.isLetterOrDigit(ch)) {
				sb.append(ch);
			} else {
				if (sb.length() > 0) words.add(sb.toString());
				sb.setLength(0);
			}
		}
		
		if (sb.length() > 0) words.add(sb.toString());
		return words;
	}
	
	public static boolean isAscii(char ch) {
		int type = Character.getType(ch);
		return type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER;
	}
	
	public static void main(String[] args) {
		String text = "发表于 6 天, 前iPad或者";
		for (String snippet: split(text)) System.out.println(snippet);
	}
}