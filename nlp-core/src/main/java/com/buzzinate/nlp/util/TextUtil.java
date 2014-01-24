package com.buzzinate.nlp.util;

import java.io.IOException;

public class TextUtil {
	private static ThreadLocal<PorterStemmer> stemmer = new ThreadLocal<PorterStemmer>() {
        public PorterStemmer initialValue() {
            return new PorterStemmer();
        }
    };

	public enum CharType { Alpha, Digit, Letter, Other}

	private TextUtil() {
	}
	
	public static int countLetters(String text) {
		int total = 0;
		boolean isLastAlpha = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (Character.isLetter(ch)) {
				if (!isLastAlpha || !isAlphaOrDigit(ch)) total++;
			}
			if (Character.isLetter(ch) && isAlphaOrDigit(ch)) isLastAlpha = true;
			else isLastAlpha = false;
		}
		return total;
	}	
	
	public static int parseInt(String text, int d) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') sb.append(ch);
			else if (sb.length() > 0) return Integer.parseInt(sb.toString());
		}
		if (sb.length() > 0) return Integer.parseInt(sb.toString());
		return d;
	}
	
	public static boolean isFirstAscii(String text) {
		if (text.length() == 0) return false;
		char ch = text.charAt(0);
		int type = Character.getType(ch);
		return type == Character.UPPERCASE_LETTER || type == Character.LOWERCASE_LETTER;
	}
	
	public static boolean isFirstDigit(String text) {
		if (text.length() == 0) return false;
		return Character.isDigit(text.charAt(0));
	}
	
	public static String stemAll(String text) {
		StringBuffer sb = new StringBuffer();
		int last = 0;
		boolean prevAscii = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			boolean ascii = isAscii(ch);
			if (prevAscii != ascii) {
				sb.append(stem(text.substring(last, i)));
				last = i;
			}
			prevAscii = ascii;
		}
		if (last < text.length()) sb.append(stem(text.substring(last, text.length())));
		return sb.toString();
	}
	
	public static String stem(String word) {
		if ("ios".equalsIgnoreCase(word)) return word;
		else return stemmer.get().stem(word);
	}

	public static boolean isAscii(char ch) {
		int type = Character.getType(ch);
		return type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER;
	}
	
	private static int charType(char ch) {
		int type = Character.getType(ch);
		if (type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER) return 1; // en
		if (type == Character.DECIMAL_DIGIT_NUMBER) return 2; // num
		return 0;
	}
	
	public static String fillText(String text) {
		text = stemAll(text.toLowerCase());
		StringBuffer sb = new StringBuffer();
		sb.append(" ");
		int prevct = 0;
		char prevch = ' ';
		for (int i = 0; i < text.length(); i++) {
			int ct = charType(text.charAt(i));
			if (prevct == 1) {
				if (ct == 0 && text.charAt(i) != '-') sb.append(" "); 
			}
			if (prevct == 2) {
				if (ct == 0 && text.charAt(i) != '.') sb.append(" ");
			}
			if (prevct == 0) {
				if (ct == 1 && prevch != '-') sb.append(" ");
				if (ct == 2 && prevch != '.') sb.append(" ");
			}
			sb.append(text.charAt(i));
			prevct = ct;
			prevch = text.charAt(i);
		}
		sb.append(" ");
		return sb.toString();
	}
	
	public static String fillWord(String word) {
		word = stemAll(fillText(word).trim());
		if (needboundary(word.charAt(0))) word = " " + word;
		if (needboundary(word.charAt(word.length() - 1))) word = word + " ";
		return word;
	}
	
	private static boolean needboundary(char ch) {
		return charType(ch) > 0 || ch == '.' || ch == '-';
	}

	public static int countPuncs(String text) {
		int nPuncs = 0;

		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == ',' || ch == '.') {
				if (i + 1 == text.length())
					nPuncs++;
				else if (Character.isWhitespace(text.charAt(i + 1)))
					nPuncs++;
			}
			if (ch == '，' || ch == '。')
				nPuncs++;
		}
		return nPuncs;
	}

	public static int countNumWords(String text) {
		int total = text.length();
		boolean isLastLetter = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (isAlphaOrDigit(ch)) {
				if (isLastLetter)
					total -= 1;
				isLastLetter = true;
			} else {
				if (!Character.isLetter(ch))
					total -= 1;
				isLastLetter = false;
			}
		}
		return total;
	}

	public static boolean isAlphaOrDigit(char ch) {
		if (ch >= 'a' && ch <= 'z')
			return true;
		if (ch >= 'A' && ch <= 'Z')
			return true;
		if (ch >= '0' && ch <= '9')
			return true;
		return false;
	}
	
	public static boolean isContainAlphaOrDigit(String str) {
		boolean isContain = false;
		for(char ch : str.toCharArray()){
			if(isAlphaOrDigit(ch)){
				isContain = true;
				break;
			}
		}
		return isContain;
	}
	
	/**
	 * 
	 * @description 查找最大公共子串
	 * @return
	 */
	public static String findLcs(String str1, String str2) {
		StringBuffer buff = new StringBuffer();
		int i, j;
		int len1, len2;
		len1 = str1.length();
		len2 = str2.length();
		int maxLen = len1 > len2 ? len1 : len2;
		int[] max = new int[maxLen];
		int[] maxIndex = new int[maxLen];
		int[] c = new int[maxLen];

		for (i = 0; i < len2; i++) {
			for (j = len1 - 1; j >= 0; j--) {
				if (str2.charAt(i) == str1.charAt(j)) {
					if ((i == 0) || (j == 0))
						c[j] = 1;
					else
						c[j] = c[j - 1] + 1;
				} else {
					c[j] = 0;
				}

				if (c[j] > max[0]) { // 如果是大于那暂时只有一个是最长的,而且要把后面的清0;
					max[0] = c[j];
					maxIndex[0] = j;

					for (int k = 1; k < maxLen; k++) {
						max[k] = 0;
						maxIndex[k] = 0;
					}
				} else if (c[j] == max[0]) { // 有多个是相同长度的子串
					for (int k = 1; k < maxLen; k++) {
						if (max[k] == 0) {
							max[k] = c[j];
							maxIndex[k] = j;
							break; // 在后面加一个就要退出循环了
						}

					}
				}
			}
		}

		for (j = 0; j < maxLen; j++) {
			if (max[j] > 0) {
				// System.out.println("第" + (j + 1) + "个公共子串:");
				for (i = maxIndex[j] - max[j] + 1; i <= maxIndex[j]; i++)
					buff.append(str1.charAt(i));
				// System.out.println(" ");
			}
		}
		return buff.toString();
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		String text = fillText("苏州轨道交通2020年画成一个“井” what is you name");
		System.out.println(text);
		System.out.println(text.contains(fillWord("20")));
		System.out.println(text.contains(fillWord("ou")));
		System.out.println(text.contains(fillWord("年画")));
		String text1 = fillText("美版iphone4晋级到5.0.1后SIM卡无法辨认 跪求高手指条明路....");
		System.out.println(text1);
		System.out.println(text1.contains(fillWord("0.1")));
		System.out.println(text1.contains(fillWord(".0.1")));
		System.out.println(text1.contains(fillWord("5.0.1")));
		System.out.println(stemAll("相比对手在 iOS 端上的大红大紫"));
		// String test = "\n\n\n\n\n\nas;lkdjfsad;lfj";
		// System.out.println(test.indexOf("asdf", 10000));
		// System.out.println(test.replaceAll("ddd", "\n"));
		//
		// String text =
		// "据彭博社报道，Google即将于Google I/O大会上发布自有品牌平板电脑Nexus 7，目标直指苹果iPad。这是继微软于上周发布自有品牌Surface平板电脑以后，Google在该领域发起的最新攻势。平板领域或许将形成苹果、微软以及Google的“三足鼎立”之势。"
		//
		// +
		// "根据我们之前的报道，Google的这款平板将由华硕代工，携带四核Tegra 3处理器，支持NFC和Google Wallet，并搭载Google最新Jelly Bean（Android 4.1）操作系统。分为8G/16G两个版本，8G版售价199美元，16G版售价249美元。而微软之前发布的平板电脑Surface则有两个版本，一款搭载ARM处理器，使用的是Windows 8专门为ARM设计的Windows RT版本。另一款搭载的是英特尔Core i5 Ivy Bridge处理器，使用Windows 8专业版。";
		// System.out.println(stemAll(text));
		
	}
}
