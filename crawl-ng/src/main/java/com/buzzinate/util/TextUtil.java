package com.buzzinate.util;

public class TextUtil {
	public static int countNumWords(String text) {
		int total = text.length();
		boolean isLastLetter = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (isAlphaOrDigit(ch)) {
				if (isLastLetter) total -= 1;
				isLastLetter = true;
			} else {
				if (!Character.isLetter(ch)) total -= 1;
				isLastLetter = false;
			}
		}
		return total;
	}
	
	public static boolean isAlphaOrDigit(char ch) {
		if (ch >= 'a' && ch <= 'z') return true;
		if (ch >= 'A' && ch <= 'Z') return true;
		if (ch >= '0' && ch <= '9') return true;
		return false;
	}
	
	public static int countPuncs(String text) {
		int nPuncs = 0;
		
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == ',' || ch == '.') {
				if (i + 1 == text.length()) nPuncs++;
				else if (Character.isWhitespace(text.charAt(i+1))) nPuncs++;
			}
			if (ch == '，' || ch == '。') nPuncs++;
		}
		return nPuncs;
	}
	
	/**
	 * 
	 * @description 查找最大公共子串
	 * @param one
	 * @param other
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
}