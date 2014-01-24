package com.buzzinate.lezhi.util;

import org.buzzinate.lezhi.util.MurmurHash3;

public class SignatureUtil {
	private SignatureUtil() {}
	
	public static String normalizeTitle(String title) {
        // remove #blz_insite or #nextPage
        int idx = title.indexOf("#");
        if (idx >= 0) {
            String after = title.substring(idx);
            if (after.startsWith("#blz") || after.startsWith("#next")) title = title.substring(0, idx);
        }

		StringBuffer sb = new StringBuffer();
		int last = 0;
		for (int i = 0; i < title.length(); i++) {
			if (!Character.isLetter(title.charAt(i))) {
				if (i > last) {
//					System.out.println(title.substring(last, i));
					sb.append(title.substring(last, i)).append("-");
				}
				last = i + 1;
			}
		}
		if (title.length() > last) {
//			System.out.println(title.substring(last));
			sb.append(title.substring(last)).append("-");
		}
		return sb.toString();
	}
	
	public static long signatureTitle(String title) {
		return MurmurHash3.hash64(normalizeTitle(title)) & Long.MAX_VALUE;
	}
	
	public static String signature(String text) {
		return Long.toHexString(MurmurHash3.hash64(text));
	}
	
	public static void main(String[] args) {
		System.out.println(signature("http://test.buzzinate.com/wordpress/?p=5019"));
		System.out.println(signatureTitle("中新博客精选--揭秘：邓小平一生都拒绝辞去的职务(2)#blz_insite"));
		System.out.println(signatureTitle("中新博客精选--揭秘：邓小平一生都拒绝辞去的职务"));
		System.out.println(signatureTitle("Read me"));
		System.out.println(signatureTitle("Readme"));
	}
}