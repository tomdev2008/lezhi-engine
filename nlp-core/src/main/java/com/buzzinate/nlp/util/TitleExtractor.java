package com.buzzinate.nlp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TitleExtractor {
	private static List<String> ignoreSuffixes = Arrays.asList("频道", "站", "网", "报", "集", "公司", ".com", ".cn", "平台", "门户", "博客", "精选", "博客精选");
	private static List<String> ignorePrefixes = Arrays.asList("Powered by");
	private static HashSet<Character> splitChars = new HashSet<Character>(Arrays.asList('|', '_', '-', '—', '－', '<', '>', '«', '»'));

	public String extract(String title) {
		if (title == null || title.trim().length() == 0) return title;

		List<String> parts = split(title);
		return getLongestPart(parts);
	}
	
	public String extractFirst(String title) {
		if (title == null || title.trim().length() == 0) return title;

		List<String> parts = split(title);
		parts.add(title);
		return parts.get(0); 
	}

	private String getLongestPart(List<String> parts) {
		double longestNumWords = 0;
		String longestPart = "";
		for (int i = 0; i < parts.size(); i++) {
			String p = parts.get(i).trim();
			
			int ignoreCount = 0;
			for (String is: ignoreSuffixes) if (p.toLowerCase().endsWith(is)) ignoreCount++;
			for (String ip: ignorePrefixes) if (p.toLowerCase().startsWith(ip)) ignoreCount++;
			int colonCnt = StringUtils.countMatches(p, ",");
			if (colonCnt > 0) ignoreCount += colonCnt - 1;
			colonCnt = StringUtils.countMatches(p, "，");
			if (colonCnt > 0) ignoreCount += colonCnt - 1;
			double numWords = TextUtil.countNumWords(p);
			numWords = numWords / (1 + 2 * ignoreCount);
			if (i == 0) numWords = numWords * 2;
            if (numWords > longestNumWords) {
            	longestNumWords = numWords;
            	longestPart = p;
            }
		}
		if (longestPart.length() == 0) return null;
		else return longestPart.trim();
	}
	
	private List<String> split(String title) {
		List<String> parts = new ArrayList<String>();
		int last = 0;
		int quoteCnt = 0;
		boolean lastLetter = false;
		boolean lastDigit = false;
		for (int i = 0; i < title.length(); i++) {
			char ch = title.charAt(i);
			int type = Character.getType(ch);
			if (type == Character.INITIAL_QUOTE_PUNCTUATION) quoteCnt++;
			if (type == Character.START_PUNCTUATION) quoteCnt++;
			if (quoteCnt == 0 && !lastLetter && !lastDigit && splitChars.contains(ch)) {
				parts.add(title.substring(last, i));
				last = i + 1;
			}
			if (type == Character.FINAL_QUOTE_PUNCTUATION) quoteCnt--;
			if (type == Character.END_PUNCTUATION) quoteCnt--;
			if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z') lastLetter = true;
			else lastLetter = false;
			if (Character.isDigit(ch)) lastDigit = true;
			else if (!splitChars.contains(ch)) lastDigit = false;
		}
		if (last < title.length()) parts.add(title.substring(last));
		return parts;
	}

	public static void main(String[] args) {
		TitleExtractor te = new TitleExtractor();
		String[] titles = new String[] {
				"Haskell/Applicative Functors - Wikibooks, open books for an open world",
				"中国新闻出版网 www.chinaxwcb.com - 鲜果读书：让阅读更“清新”一点",
				"清润饮食“熄灭”冬季之火 - 素食 - 大渡网-佛教资讯，生活，人文，心灵感悟，佛艺时尚杂志，佛教音乐，佛教常识，佛教视频",
				"【WOLF字幕組】★4月新番【少年同盟 II_kimi to boku II】【第07話 Sweet sweet,bitter】[BIG5][1024X576][MP4] - 新番连载 - 动漫下载|BT|漫画|动画|游戏 - 极影动漫",
				"从草根到精英——大陆网络民族主义流变-观点评论-时事评论-四月网-青年思想门户-M4.CN",
				"你所不知道的铁三角 ATH-CKM1000简评-耳机耳塞-iMP3随身影音",
				"测测你穿越后变成谁 - 美丽说，发现、收藏、分享我的美丽点滴，让改变发生",
				"我发现了“APP 僵尸农场”很不错，你也来看看吧：“http://site.ruanlie.com/latest/app/cn_411657026.html?subUri=latest”（软件猎手官方微博：http://weibo.com/woaiphone）",
				"“怪”邻居-“寻找勇士”-牛士勇士故事大征集",
				"上网流量查询器_流量监控_广西3G应用专区"
		};
		for (String title: titles) {
			System.out.println(title + " ==> " + te.extract(title));
		}
	}

}

