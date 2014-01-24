package com.buzzinate.lezhi.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

public class UpYunUtil {
	private static SimpleDateFormat formater = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);

	static {
		formater.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * 获取 GMT 格式时间戳 return String;
	 */
	public static String getGMTDate() {
		return formater.format(new Date());
	}

	/**
	 * MD5 加密方法
	 * 
	 * @param str
	 *            待加密字符串 return 加密后字符串;
	 */
	public static String md5(String str) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		md5.update(str.getBytes());
		byte[] encodedValue = md5.digest();
		int j = encodedValue.length;
		char finalValue[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte encoded = encodedValue[i];
			finalValue[k++] = hexDigits[encoded >> 4 & 0xf];
			finalValue[k++] = hexDigits[encoded & 0xf];
		}

		return new String(finalValue);
	}

	public static String md5(byte[] bs) throws Exception {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.update(bs, 0, bs.length);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

		byte[] encodedValue = md5.digest();

		int j = encodedValue.length;
		char finalValue[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte encoded = encodedValue[i];
			finalValue[k++] = hexDigits[encoded >> 4 & 0xf];
			finalValue[k++] = hexDigits[encoded & 0xf];
		}

		return new String(finalValue);
	}

	public static String md5(File file) throws Exception {
		FileInputStream is = new FileInputStream(file);
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			int n = 0;
			byte[] buffer = new byte[1024];
			do {
				n = is.read(buffer);
				if (n > 0) {
					md5.update(buffer, 0, n);
				}
			} while (n != -1);
			is.skip(0);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			is.close();
		}

		byte[] encodedValue = md5.digest();

		int j = encodedValue.length;
		char finalValue[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte encoded = encodedValue[i];
			finalValue[k++] = hexDigits[encoded >> 4 & 0xf];
			finalValue[k++] = hexDigits[encoded & 0xf];
		}

		return new String(finalValue);
	}

	/**
	 * 连接签名方法
	 * 
	 * @param conn
	 *            连接
	 * @param uri
	 *            请求地址
	 * @param length
	 *            请求所发Body数据长度 return 签名字符串
	 */
	public static String sign(String method, String uri, String date,
			long length, String username, String password) {
		String sign = method + "&" + uri + "&" + date + "&" + length + "&"
				+ password;
		// System.out.println(sign);
		// System.out.println("UpYun " + username + ":" + md5(sign));
		return "UpYun " + username + ":" + UpYunUtil.md5(sign);
	}

	public static String upyunUrl(String bucketname, String url, String imgSrc) {
		String upyunUrl = "";
		if (StringUtils.isNotEmpty(imgSrc)) {
			String lowercaseImgSrc = imgSrc.toLowerCase();
			String format = "";
			if (lowercaseImgSrc.endsWith(".jpg")
					|| lowercaseImgSrc.endsWith(".jpeg"))
				format = "jpeg";
			else if (lowercaseImgSrc.endsWith(".png"))
				format = "png";
			else if (lowercaseImgSrc.endsWith("gif"))
				format = "gif";
			upyunUrl = "http://"
					+ bucketname
					+ ".b0.upaiyun.com/"
					+ StringUtils.replaceChars(DomainNames.safeGetPLD(url),
							'.', '_') + "/" + UpYunUtil.md5(imgSrc) + "."
					+ format;
		}
		return upyunUrl;
	}

	public static void main(String[] args) {		
		System.out
				.println(md5("http://imgmil.gmw.cn/attachement/jpg/site2/20120302/b8ac6f402aaf10ba6a7309.jpg"));
	}
}