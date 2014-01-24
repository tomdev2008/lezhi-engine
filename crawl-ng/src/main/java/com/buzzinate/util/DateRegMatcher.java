package com.buzzinate.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;

public class DateRegMatcher {
	// private static String dateReg_1 =
	// "\\d{2,4}[\\-|\\/|\\.|年]\\d{1,2}[\\-|\\/|\\.|月]\\d{1,2}[日]?(\\s\\d{1,2}\\:\\d{1,2}(:\\d{1,2})?)?";
	/** YYYY[/|-|.|年](MM|M)[-|.|月](DD|D)(日)?* */
	private static String date1 = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})[\\/|\\-|\\.|年](((1[02]|0?[13578])[\\/|\\-|\\.|月]([12][0-9]|3[01]|0?[1-9])日?)|((11|0?[469])[\\/|\\-|\\.|月]([12][0-9]|30|0?[1-9])日?)|((0?2)[\\/|\\-|\\.|月]([1][0-9]|2[0-8]|0?[1-9])日?)))"
			+ "|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))[\\/|\\-|\\.|年]0?2[/|-|.|月]29日?)";
	/** [DD|D]/[MM|M]/YYYY* */
	private static String date2 = "((([12][0-9]|3[01]|0?[1-9])[\\/|\\-|\\.](1[02]|0?[13578]))|(([12][0-9]|30|0?[1-9])[\\/|\\-|\\.](11|0?[469]))|(([1][0-9]|2[0-8]|0?[1-9])[\\/|\\-|\\.]0?2))[\\/|\\-|\\.]([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})|"
			+ "(29[\\/|\\-|\\.]0?2[\\/|\\-|\\.](([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00)))";
	/** [MM|M][/|.|-][DD|D][/|.|-]YYYY* */
	private static String date3 = "(((1[02]0?[13578])[\\/|\\-|\\.]([12][0-9]|3[01]0?[1-9]))|((11|0?[469])[\\/|\\-|\\.]([12][0-9]|30|0?[1-9]))|(0?2[\\/|\\-|\\.]([1][0-9]|2[0-8]|0?[1-9])))[\\/|\\-|\\.]([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})|"
			+ "(29[\\/|\\-|\\.]0?2[\\/|\\-|\\.](([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00)))";

	private static final Pattern dateReg = Pattern.compile("((" + date1 + ")|(" + date2 + ")|(" + date3 + "))");
	
	private static final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

	public DateRegMatcher() {
	}

	/**
	 * @description 日期正则表达式匹配
	 */
	public static String matcher(String text) {
		String result = "";
		Matcher m = dateReg.matcher(text);
		if (m.find()) {
			result = m.group();
				// if (result.substring(0, 4).indexOf("/") > 0
				// || result.substring(0, 4).indexOf("-") > 0
				// || result.substring(0, 4).indexOf("-") > 0)
				// result = (new StringBuilder("20")).append(result)
				// .toString();
			if (result.indexOf("年") > 0) {
				StringBuffer buff = new StringBuffer();
				buff.append(result.substring(0, result.indexOf("年")));
				if (result.indexOf("月") > 0) {
					buff.append("-"+ result.substring(result.indexOf("年") + 1, result.indexOf("月")));
				}
				if (result.indexOf("日") > 0) {
					buff.append("-"+ result.substring(result.indexOf("月") + 1, result.indexOf("日")));
					buff.append(result.substring(result.indexOf("日") + 1));
					result = buff.toString();
				}
			}
		}
		
		return getChange(result);
	}

	public static long getTime(String date, String url) {
		long time = -1L;
		try {
			time = format.parse(date).getTime();
		} catch (ParseException e) {
			try {
				Date parsedDate = DateUtils.parseDate(date,
						new String[] { "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss",
								"yyyy/MM/dd HH:mm", "yyyy-MM-dd",
								"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
								"yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss",
								"yyyy.MM.dd HH:mm" });
				time = parsedDate.getTime();
			} catch (Exception e2) {
				System.err.println(url + ": can't parse erroneous date: " + date);
			}
		}
		return time;
	}

	/**
	 * 
	 * @description 将MM/dd/yyyy 转化为yyyy/MM/dd
	 * @param dateStr
	 * @return
	 */
	public static String getChange(String dateStr) {
		if (dateStr.equalsIgnoreCase("")) {
			return "";
		} else {
			String[] temp = dateStr.split("/");
			if (temp.length <= 2) {
				return dateStr;
			} else {
				if (temp[0].length() == 4) {
					return dateStr;
				} else {
					int pos = temp[2].indexOf(" ");
					if (pos > 0) {
						return temp[2].substring(0, pos) + "/" + temp[0] + "/" + temp[1] + " " + temp[2].substring(pos + 1);
					} else {
						return temp[2] + "/" + temp[0] + "/" + temp[1];
					}
				}
			}
		}
	}

	public static void main(String args[]) {
//		String[] example = { "2007/2/10", "2007/2/9", "2007/02/10",
//				"2007/02/9", "2007/2/10 15:30", "2007/2/9 14:20:20",
//				"2007-2-10", "2007-2-9", "2007-02-10", "2007-02-9",
//				"2007-2-10 14:20", "2007-2-9 9:30:20", "2007.2.10", "2007.2.9",
//				"2007.02.10", "2007.02.9", "2007.2.10 10:30",
//				"2007.2.9 9:30:20", "2007年2月10日",
//				"2007年2月9日",
//				// "20.02.2007","9.02.2007","20.02.2007","09.02.2007",
//				// "20.02.2007 15:30","9.02.2007 15:30:20",
//				"20/02/2007", "9/02/2007", "20/02/2007", "09/02/2007",
//				"20/02/2007 15:30", "9/02/2007 15:30:20", };
//		String reg = "";
//		for (int i = 0; i < example.length; i++) {
//			String result = matcher(example[i], "");
//			System.out.println(result);
//			long time = getTime(result, null);
//			System.out.println(new Date(time));
//		}
//		String ss = "2007-09-10";
//		System.out.println(getChange(ss));
		// SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		// try {
		// System.out.println(df.parse("20/02/2007"));
		// } catch (ParseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		long time=-1L;
		String text="无人岛许愿2012-06-20 12:08小编 中文名上海海隆软件股份有限公司英文名Shanghai Hyron Software Co., Ltd公司简介上海海隆软件股份有限公司（原上海交大海隆软件股份有限公司）为上海市著名软件企业、国家规划布局内重点软件企业，主要股东为上海交通大学、日本欧姆龙株式会社及经营管理层。公司注册资金4290万元，集团企业共有员工780人，95%以上是计算机相关专业的技术人员，其中绝大部分员工为本科以上学历，15%以上具有硕士以上学位，40%以上员工有在日本工作和研修的经历。公司目前已正积极筹备在深圳中小企业板上市。希望能以自己的成长为中国软件产业的发展奉献绵薄之力。服务/产品对日软件开发公司网站http://www.hyron.com公司总部地址：上海市肇嘉浜路1033号徐家汇国际大厦5楼地址：上海市天钥桥路1号煤科大厦15楼地址：上海市徐汇区肇嘉浜路680号金钟大厦6楼邮编：200030电话：021-64689626 021-64878056传真：021-64689489网址：http://www.hyron.com？Email：planning@hyron.com？分公司分布上海、日本目标市场日本覆盖行业银行、保险、证券、手机通信、物流、交通控制、Internet系统及业务应用系统等投资者上海交通大学、日本欧姆龙株式会社及经营管理层员工数量780管理层包叔平：董事长、总经理、日本海隆株式会社董事长齐藤肇：日本海隆株式会社董事、总经理潘世雷：董？事、副总经理、第二海外事业担当陆？？庆：董？事、副总经理，第一海外事业担当获得奖项2006年度获上海市小巨人企业称号2002—2006年连续5年荣获“国家规划布局内重点软件企业”称号中文名上海海隆软件股份有限公司英文名Shanghai Hyron Software Co., Ltd公司简介上海海隆软件股份有限公司（原上海交大海隆软件股份有限公司）为上海市著名软件企业、国家规划布局内重点软件企业，主要股东为上海交通大学、日本欧姆龙株式会社及经营管理层。公司注册资金4290万元，集团企业共有员工780人，95%以上是计算机相关专业的技术人员，其中绝大部分员工为本科以上学历，15%以上具有硕士以上学位，40%以上员工有在日本工作和研修的经历。公司目前已正积极筹备在深圳中小企业板上市。希望能以自己的成长为中国软件产业的发展奉献绵薄之力。服务/产品对日软件开发公司网站http://www.hyron.com公司总部地址：上海市肇嘉浜路1033号徐家汇国际大厦5楼地址：上海市天钥桥路1号煤科大厦15楼地址：上海市徐汇区肇嘉浜路680号金钟大厦6楼邮编：200030电话：021-64689626 021-64878056传真：021-64689489网址：http://www.hyron.com？Email：planning@hyron.com？分公司分布上海、日本目标市场日本覆盖行业银行、保险、证券、手机通信、物流、交通控制、Internet系统及业务应用系统等投资者上海交通大学、日本欧姆龙株式会社及经营管理层员工数量780管理层包叔平：董事长、总经理、日本海隆株式会社董事长齐藤肇：日本海隆株式会社董事、总经理潘世雷：董？事、副总经理、第二海外事业担当陆？？庆：董？事、副总经理，第一海外事业担当获得奖项2006年度获上海市小巨人企业称号2002—2006年连续5年荣获“国家规划布局内重点软件企业”称号";
		String pd=DateRegMatcher.matcher(text);
		time=DateRegMatcher.getTime(pd,"");
		System.out.println(time);
	}

}
