package org.ansj.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.ansj.dic.DicReader;
import org.ansj.domain.BigramEntry;

/**
 * 这个类储存一些公用变量.
 * 
 * @author ansj
 * 
 */
/**
 * @author Frank
 *
 */
public class MyStaticValue {

	public static String userDefinePath = null;

	/**
	 * 人名词典
	 * 
	 * @return
	 */
	public static BufferedReader getPersonReader() {
		return DicReader.getReaderResource("person/person.dic");
	}
	
	/**
	 * 机构名词典
	 * 
	 * @return
	 */
	public static BufferedReader getCompanReader() {
		return DicReader.getReader("company/company.data");
	}

	/**
	 * 核心词典
	 * 
	 * @return
	 */
	public static BufferedReader getArraysReader() {
		return DicReader.getReaderResource("arrays.dic");
	}

	/**
	 * 数字词典
	 * 
	 * @return
	 */
	public static BufferedReader getNumberReader() {
		return DicReader.getReaderResource("numberLibrary.dic");
	}

	/**
	 * 英文词典
	 * 
	 * @return
	 */
	public static BufferedReader getEnglishReader() {
		return DicReader.getReaderResource("englishLibrary.dic");
	}

	/**
	 * 词性表
	 * 
	 * @return
	 */
	public static BufferedReader getNatureMapReader() {
		return DicReader.getReaderResource("nature/nature.map");
	}

	/**
	 * 词性关联表
	 * 
	 * @return
	 */
	public static BufferedReader getNatureTableReader() {
		return DicReader.getReaderResource("nature/nature.table");
	}
	

	/**
	 * 得道姓名单字的词频词典
	 * 
	 * @return
	 */
	public static BufferedReader getPersonFreqReader() {
		return DicReader.getReaderResource("person/name_freq.dic");
	}

	/**
	 * 名字词性对象反序列化
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, int[][]> getPersonFreqMap() {
		InputStream inputStream = null;
		ObjectInputStream objectInputStream = null;
		Map<String, int[][]> map = new HashMap<String, int[][]>(0);
		try {
			//inputStream = DicReader.getInputStream("person/asian_name_freq.data");
			inputStream = DicReader.getInputStream("person/asian_name_freq.data");
			objectInputStream = new ObjectInputStream(inputStream);
			map = (Map<String, int[][]>) objectInputStream.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DicReader.closeQuitely(objectInputStream);
			DicReader.closeQuitely(inputStream);
		}
		return map;
	}

	/**
	 * 词与词之间的关联表数据
	 * 
	 * @return
	 */
	public static BigramEntry[][] getBigramTables() {
		InputStream inputStream = null;
		ObjectInputStream objectInputStream = null;
		BigramEntry[][] bigramTables = new BigramEntry[0][0];
		try {
			//inputStream = DicReader.getInputStream("bigramdict.data");
			inputStream = DicReader.getInputStream("bigramdict.data");
			objectInputStream = new ObjectInputStream(inputStream);
			bigramTables = (BigramEntry[][]) objectInputStream.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DicReader.closeQuitely(objectInputStream);
			DicReader.closeQuitely(inputStream);
		}
		return bigramTables;
	}
}
