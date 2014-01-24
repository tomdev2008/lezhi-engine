package com.buzzinate.lezhi.util;

public class FastUtil {
	private static double log[] = new double[20];
	
	public static double log(int x) {
		if (x >= 0 && x < log.length) return log[x];
		else return Math.log(x);
	}
	
	public static double diffPath(String[] parts, String[] parts2) {
		int i = 0;
		double diff = 0;
		while (i < parts.length && i < parts2.length) {
			if (!parts[i].equals(parts2[i])) diff += log(1 + i);
			i += 1;
		}
		if (parts.length > i) diff += (parts.length - i) * 2;
		if (parts2.length > i) diff += (parts2.length - i) * 2;
		return diff;
	}
	
	public static String formatNum(String str) {
		StringBuffer sb = new StringBuffer();
		boolean prevDigit = false;
		int last = 0;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (Character.isDigit(ch) || ch == '_') {
				if (!prevDigit) {
					sb.append(str.substring(last, i));
					last = i;
				}
				prevDigit = true;
			} else {
				if (prevDigit) {
					sb.append("#");
					last = i;
				}
				prevDigit = false;
			}
		}
		if (last < str.length()) {
			if (prevDigit) sb.append("#");
			else sb.append(str.substring(last));
		}
		return sb.toString();
	}
	
	static {
		for (int i = 0; i < log.length; i++) log[i] = Math.log(i);
	}
}