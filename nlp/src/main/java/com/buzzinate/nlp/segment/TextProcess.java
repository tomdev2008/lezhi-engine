package com.buzzinate.nlp.segment;

import com.buzzinate.nlp.util.TextUtil;

public class TextProcess {
	public static String normalize(String text) {
		return TextUtil.stemAll(text.toLowerCase());
	}
}