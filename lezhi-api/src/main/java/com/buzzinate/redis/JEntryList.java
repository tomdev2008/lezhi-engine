package com.buzzinate.redis;

import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JEntryList<E> {	
	public List<E> values;
	public long cacheTime;
	
	public JEntryList() {
	}
	
	public JEntryList(List<E> values) {
		this(values, System.currentTimeMillis());
	}
	
	public JEntryList(List<E> values, long cacheTime) {
		this.values = values;
		this.cacheTime = cacheTime;
	}
	
	public String toJson() {
		JSONArray vs = (JSONArray) JSON.toJSON(values);
		JSONObject json = new JSONObject();
		json.put("values", vs);
		json.put("cacheTime", cacheTime);
		return json.toJSONString();
	}
	
	public static <T> JEntryList<T> parseJson(String text, Class<T> clazz) {
		JSONObject json = JSON.parseObject(text);
		long cacheTime = json.getLongValue("cacheTime");
		JSONArray vs = json.getJSONArray("values");
		List<T> values = JSON.parseArray(vs.toJSONString(), clazz);
		return new JEntryList<T>(values, cacheTime);
	}
	
	public static void main(String[] args) {
		JEntryList<TestEntry1> jes1 = new JEntryList<TestEntry1>(Arrays.asList(new TestEntry1(1, "data 1"), new TestEntry1(2, "data 2")));
		String text = jes1.toJson();
		System.out.println(text);
		JEntryList<TestEntry> jes = JEntryList.parseJson(text, TestEntry.class);
		System.out.println(jes.values.get(1).score);
	}
}

class TestEntry {
	public long id;
	public String data;
	public double score;
	
	public TestEntry() {
		
	}
	
	public TestEntry(long id, String data, double score) {
		this.id = id;
		this.data = data;
		this.score = score;
	}
}

class TestEntry1 {
	public long id;
	public String data;
	
	public TestEntry1() {
		
	}
	
	public TestEntry1(long id, String data) {
		this.id = id;
		this.data = data;
	}
}