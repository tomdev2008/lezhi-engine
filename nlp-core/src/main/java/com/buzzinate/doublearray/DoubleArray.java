package com.buzzinate.doublearray;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class DoubleArray {
	/**
	 * base: 数组用来存放单词的转换..其实就是一个DFA转换过程
	 */
	private int[] base = null;
	/**
	 * check: 数组用来验证里面存储的是上一个状态的位置
	 */
	public int[] check = null;
	/**
	 * status: 用来判断一个单词的状态 1.为不成词.处于过度阶段 2.成次也可能是词语的一部分. 3.词语结束 example: 中 1 中华
	 * 2 中华人 1 中华人民 3
	 */
	public byte[] status = null;
	
	public int[] indexes = null;
	public int size = 0;
	
	public static void main(String[] args) throws IOException {
		DoubleArray da = new DoubleArray(new FileInputStream("test.array"));
		System.out.println(da.getWordIdx("内存"));
		System.out.println(da.getWordIdx("词典"));
		System.out.println(da.getWordIdx("词语"));
		System.out.println(da.getWordIdx("词语a"));
		System.out.println(da.getWordIdx("内存"));
		System.out.println(da.getWordIdx("the"));
		System.out.println(da.getWordIdx("from"));
	}
	
	public DoubleArray(InputStream arrayIs) throws IOException {
		List<String> lines = IOUtils.readLines(arrayIs, "UTF-8");
		IOUtils.closeQuietly(arrayIs);
		
		String last = null;
		for (String line: lines) last = line;
		
		String[] strs = last.split("\t");
		int arrayLength = Integer.parseInt(strs[0]) + 1;
		base = new int[arrayLength];
		check = new int[arrayLength];
		status = new byte[arrayLength];
		indexes = new int[arrayLength];
		
		for (String line: lines) {
			strs = line.split("\t");
			int num = Integer.parseInt(strs[0]);
			base[num] = Integer.parseInt(strs[2]);
			check[num] = Integer.parseInt(strs[3]);
			status[num] = Byte.parseByte(strs[4]);
			if (status[num] >= 2) {
				indexes[num] = size;
				size += 1;
			}
		}
	}
	
	public int size() {
		return size;
	}
	
	public int getWordIdx(String str) {
		if (StringUtils.isBlank(str)) return -1;
		int baseValue = str.charAt(0);
		int checkValue = 0;
		for (int i = 1; i < str.length(); i++) {
			checkValue = baseValue;
			baseValue = base[baseValue] + str.charAt(i);
			if (baseValue > check.length - 1)
				return -1;
			if (check[baseValue] != -1 && check[baseValue] != checkValue) {
				return -1;
			}
		}
		return indexes[baseValue];
	}
}