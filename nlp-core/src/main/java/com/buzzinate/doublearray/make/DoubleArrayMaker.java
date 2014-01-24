package com.buzzinate.doublearray.make;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public class DoubleArrayMaker {
	// 终极目标
	public int base[] = new int[1000000];
	// 验证数组
	public int check[] = new int[1000000];
	// 词语状态
	public byte status[] = new byte[1000000];
	// 当前数组的词
	public String terms[] = new String[1000000];
	
	public static void main(String[] args) throws Exception {
		Set<String> words = new HashSet<String>();
		words.add("the");
		words.add("from");
		words.add("内存");
		words.add("词典");
		words.add("词语");
		new DoubleArrayMaker().make(words, "test.array");
	}
	
	public void make(Set<String> words, String arrayfile) throws Exception {
		//在内存中生成Branch词典
		List<Branch> head = new ArrayList<Branch>();
		head.add(makeBranches(words));
		List<Branch> all = treeToLibrary(head, 0, 1);
		//将head移除
		all.remove(0) ;
		makeBaseArray(all);
		writeLibrary(arrayfile);
	}
	
	/**
	 * 将生成的数组写成词典文件
	 */
	public void writeLibrary(String arrayfile) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < base.length; i++) {
			if (base[i] > 0) {
				sb.append(i + "\t" + terms[i] + "\t" + base[i] + "\t" + check[i] + "\t" + status[i]);
				sb.append("\n");
			}
		}
		System.out.println("write ok in " + arrayfile);
		FileOutputStream os = new FileOutputStream(arrayfile);
		IOUtils.write(sb, os, "UTF-8");
		IOUtils.closeQuietly(os);
	}
	
	/**
	 * 数组的生成
	 */
	private void makeBaseArray(List<Branch> all) throws Exception {
		int previous = 0;
		Map<String, Branch> tempStringMap = new HashMap<String, Branch>();
		
		char[] chars = null;
		int length = 0;
		Branch tempValueResult;
		int tempBase = 0;
		String temp = null;
		Branch branch = null;
		for (int i = 0; i < all.size(); i++) {
			branch = all.get(i);
			temp = branch.getValue();
			chars = temp.toCharArray();
			length = chars.length;
			if (length == 1) {
				base[chars[0]] = 65536;
				check[chars[0]] = -1;
				status[chars[0]] = branch.getStatus();
				terms[chars[0]] = temp;
			} else {
				int previousCheck = getBaseNum(chars);
				if (previous == previousCheck) {
					tempStringMap.put(temp, branch);
					continue;
				}
				if (tempStringMap.size() > 0) {
					setBaseValue(tempStringMap, previous);
					Iterator<Branch> it = tempStringMap.values().iterator();
					while (it.hasNext()) {
						tempValueResult = it.next();
						chars = tempValueResult.getValue().toCharArray();
						tempBase = base[previous] + chars[chars.length - 1];
						base[tempBase] = tempBase;
						check[tempBase] = previous;
						status[tempBase] = tempValueResult.getStatus();
						terms[tempBase] = tempValueResult.getValue();
					}
				}
				previous = previousCheck;
				tempStringMap = new HashMap<String, Branch>();
				tempStringMap.put(temp, branch);

			}
		}
		if (tempStringMap.size() > 0) {
			setBaseValue(tempStringMap, previous);
			Iterator<Branch> it = tempStringMap.values().iterator();
			while (it.hasNext()) {
				tempValueResult = it.next();
				chars = tempValueResult.getValue().toCharArray();
				tempBase = base[previous] + chars[chars.length - 1];
				base[tempBase] = tempBase;
				check[tempBase] = previous;
				status[tempBase] = tempValueResult.getStatus();
				terms[tempBase] = tempValueResult.getValue();
			}
		}
	}

	public void setBaseValue(Map<String, Branch> tempStringMap, int tempBase) {
		Iterator<String> it = tempStringMap.keySet().iterator();
		while (it.hasNext()) {
			char[] chars = it.next().toCharArray();
			if (!isHave(base[tempBase] + chars[chars.length - 1])) {
				base[tempBase]++;
				it = tempStringMap.keySet().iterator();
			}
		}
	}
	
	/**
	 * 判断在base数组中这个位置是否有这个对象昂
	 * 
	 * @param num
	 *            base数组中的位置
	 * @return
	 */
	public boolean isHave(int num) {
		if (base[num] > 0) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 找到该字符串上一个的位置字符串上一个的位置
	 * 
	 * @param chars
	 *            传入的字符串char数组
	 * @return
	 */
	private int getBaseNum(char[] chars) {
		int tempBase = 0;
		if (chars.length == 2) {
			return chars[0];
		}
		for (int i = 0; i < chars.length - 1; i++) {
			if (i == 0) {
				tempBase += chars[i];
			} else {
				tempBase = base[tempBase] + chars[i];
			}
		}
		return tempBase;
	}
	
	private static List<Branch> treeToLibrary(List<Branch> all, int begin, int end) {
		int beginNext = end;
		for (int i = begin; i < end; i++) {
			Branch[] branches = all.get(i).branches;
			for (int j = 0; j < branches.length; j++) {
				all.add(branches[j]);
			}
		}
		int endNext = all.size();
		if (begin != end) {
			treeToLibrary(all, beginNext, endNext);
		}
		return all;
	}
	
	private static Branch makeBranches(Set<String> words) {
		Branch head = new Branch('h', 0, null);
		Branch branch = head;
		boolean hasNext = true;
		boolean isWords = true;
		for (String word: words) {
			char[] chars = word.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (chars.length == (i + 1)) {
					isWords = true;
					hasNext = false;
				} else {
					isWords = false;
					hasNext = true;
				}
				int status = 1;
				if (isWords && hasNext) {
					status = 2;
				}

				if (!isWords && hasNext) {
					status = 1;
				}

				if (isWords && !hasNext) {
					status = 3;
				}
				branch.add(new Branch(chars[i], status, new String(chars,0,i+1)));
				branch = (Branch) branch.get(chars[i]);
			}
			branch = head;
		}
		return head;
	}
}