package org.ansj.splitWord;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.library.InitDictionary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.recognition.NatureRecognition;

public class Segment {
	public static List<Term> splitRaw(String text) throws IOException {
		return splitRaw(text, ToAnalysis.ALL);
	}
	
	public static int freq(String word) {
		return InitDictionary.freq(word);
	}
	
	public static List<Term> splitRaw(String text, int option) throws IOException {
		List<Term> result = new ArrayList<Term>();
		ToAnalysis udf = new ToAnalysis(new StringReader(text), option);
		Term term = udf.next();
		while (term != null) {
			result.add(term);
			term = udf.next();
		}
		return result;
	}
	
	public static List<Term> split(String text) throws IOException {
		return split(text, ToAnalysis.ALL);
	}
	
	public static List<Term> split(String text, int option) throws IOException {
		List<Term> result = splitRaw(text, option);
		new NatureRecognition(result).recognition();
		return result;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(split("范冰冰为某杂志拍封面"));
		System.out.println(split("陆毅微博晒三岁女儿萝莉照片"));
		System.out.println(split("三星i9100真伪查询, 来源于有助于有利于意味着使中国让子弹飞, 函数正是一个例子, 美版iphone4..."));
		System.out.println(split("狄波拉大赞张柏芝好媳妇"));
		System.out.println(split("2012年12月8日2小时19分钟, 这辆车,三个人,不可否认, 让人产生程序员不是一般人，对不起，我为什么,  缺点是，也就是说，茄汁鱼卷"));
		System.out.println(split("刘德华爱上梁朝伟", ToAnalysis.USE_USER_DEFINE));
		System.out.println(split("亚马逊云平台（Amazon AWS）备受青睐，一方面是一个云平台上", ToAnalysis.USE_USER_DEFINE));
		System.out.println(splitRaw("2011-34-43 为中国定制 英菲尼迪M35hL北京车展首发"));
		
		String[] texts = new String[] {
			"《星际传奇3》首曝剧照 光头迪塞尔身着铁甲钢盔",
			"优酷土豆宣布合并",
			"话说操作系统也越来越大了",
			"婚后三年考察期不能过性生活",
			"可以兼容前端各种浏览器吗",
			"经常遭遇服务不稳定的情形",
			"母亲冲进浴室给女儿拍视频征婚",
			"都是艺术",
			"这个关键词十八个人",
			"十八大代表中出现许多“草根代表”，充分体现了党在十八大代表选举过程中注重基层工作一线的导向，是民心所向、民意所归。",
			"11月8日，波兰第一位变性国会议员宣誓就职",
			"大S婆婆张兰昨中午还被目击和S妈一起在阪急Afternoon Tea吃饭",
			"韩媒：中国SR-5型火箭炮代表世界最高水平(图)",
			"西门子",
			"纯法系爆发"
		};
		for (String t: texts) {
			System.out.println(split(t));
		}
	}
}