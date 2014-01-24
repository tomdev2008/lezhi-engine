package com.buzzinate.nlp.dlg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class AtomFreq {
	private Map<String, Integer> charCnt = new HashMap<String, Integer>();
	private int nChar = 0;
	private double dl = 0;
	
	public AtomFreq(Map<String, Integer> charFreq) {
		this.charCnt = charFreq;
		for (Map.Entry<String, Integer> e: charFreq.entrySet()) {
			nChar += e.getValue();
		}
		for (Map.Entry<String, Integer> e: charCnt.entrySet()) {
			int cx = e.getValue();
			dl += cx * Math.log(nChar * 1d / cx);
		}
	}
	
	public static void main(String[] args) {
		HashMap<String, Integer> cf = new HashMap<String, Integer>();
		cf.put("A", 2);
		cf.put("B", 2);
		cf.put("C", 1);
		AtomFreq charFreq = new AtomFreq(cf);
		double dlg = charFreq.dlg(Arrays.asList("A", "B"), 2);
		System.out.println(dlg);
	}
	
	public double dlg(List<String> nx, int freq) {
		double dlX = 0, c_nx = freq;
		double CX = nChar - c_nx * nx.size() + c_nx + nx.size() + 1;
//		System.out.println("total: " + nChar + " => " + CX);
		
		for(Map.Entry<String, Integer> e: charCnt.entrySet()) {
			String x = e.getKey();
			double c_xo = e.getValue();
			double c_x;
			
			if(nx.indexOf(x)==-1 || String.valueOf(x).equals(nx))
				c_x = c_xo;
			else{
				double cnt = countMatches(nx, x);
				c_x = c_xo - c_nx * cnt + cnt;
//				double eno = c_xo * Math.log(nChar / c_xo);
//				double en = c_x * Math.log(CX / c_x);
//				System.out.println(x + ": " + c_xo + ", " + eno + " => " + c_x + ", " + en + " => " + (en - eno));
			}
			dlX += c_x * Math.log(CX / c_x);
		}
		
		dlX += c_nx * Math.log(CX / c_nx);
//		System.out.println(nx + ": " + 0 + " => " + c_nx + ", " + c_nx * Math.log(CX / c_nx));
//		dlX -= -Math.log(CX);
		
		return (dl - dlX) / c_nx;
	}
	
	private int countMatches(List<String> nx, String x) {
		int cnt = 0;
		for (String str: nx) {
			if (str.equals(x)) cnt ++;
		}
		return cnt;
	}
	
	public double dlg(String nx, int freq) {
		double dlX = 0, c_nx = freq;
		double CX = nChar - c_nx * nx.length() + c_nx + nx.length() + 1;
//		System.out.println("total: " + nChar + " => " + CX);
		
		for(Map.Entry<String, Integer> e: charCnt.entrySet()) {
			String x = e.getKey();
			double c_xo = e.getValue();
			double c_x;
			
			if(nx.indexOf(x)==-1 || String.valueOf(x).equals(nx))
				c_x = c_xo;
			else{
				double cnt = StringUtils.countMatches(nx, x);
				c_x = c_xo - c_nx * cnt + cnt;
//				double eno = c_xo * Math.log(nChar / c_xo);
//				double en = c_x * Math.log(CX / c_x);
//				System.out.println(x + ": " + c_xo + ", " + eno + " => " + c_x + ", " + en + " => " + (en - eno));
			}
			dlX += c_x * Math.log(CX / c_x);
		}
		
		dlX += c_nx * Math.log(CX / c_nx);
//		System.out.println(nx + ": " + 0 + " => " + c_nx + ", " + c_nx * Math.log(CX / c_nx));
		dlX -= -Math.log(CX);
		
		return (dl - dlX) / c_nx;
	}
	
	public int getTotalChar() {
		return nChar;
	}
	
	public int freq(String ch) {
		return charCnt.get(ch);
	}
}