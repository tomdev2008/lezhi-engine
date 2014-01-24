package org.ansj.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.ansj.dic.DicReader;

/**
 * @author peifei
 *
 */
public class StopWords {
	
	public static Set<String>  hs = new HashSet<String>();
	public StopWords(){
		if(hs == null){
			getStopWords();
		}
	}
	public static void getStopWords() {
		BufferedReader filter = DicReader.getReaderResource("library/stop/stopLibrary.dic");
		String temp = null;
		try {
			while ((temp = filter.readLine()) != null) {
				temp = temp.trim().toLowerCase();
				hs.add(temp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static boolean judgeWords(Set<String>set,String word){
		
		for(String sw: set){
			if(word.startsWith(sw)|| word.endsWith(sw) ||word.indexOf(sw) != -1){
				return false;
			}
		}
		return true;
	}

}
