package org.ansj.dic;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 加载词典用的类
 * 
 * @author ansj
 * @Frank 添加读取词典文件接口
 */
public class DicReader {
	private static Charset utf8 = Charset.forName("UTF-8");
	
	public static BufferedReader getReader(String name) {
		return new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(name), utf8));
	}
	
	public static List<String> getLines(String name) {
		List<String> lines = new ArrayList<String>();
		BufferedReader br = getReader(name);
		try {
			String line = br.readLine();
			while (line != null) {
				lines.add(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuitely(br);
		}
		return lines;
	}

	public static InputStream getInputStream(String name) {
		return new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(name));
	}

	public static BufferedReader getReaderResource(String filename) {
		return getReader(filename);
	}
	
	public static void closeQuitely(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {}
		}
	}
}