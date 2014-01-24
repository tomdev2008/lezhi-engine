package com.buzzinate.json;

public class RecItem { 
	public String url;
	public String title;
	public String thumbnail;
	public long lastModified;
	public long click;
	public float score;
	
	public RecItem() {
	}
	
	public RecItem(String url, String title, String thumbnail, long lastModified, long click, float score) {
		this.url = url;
		this.title = title;
		this.thumbnail = thumbnail;
		this.lastModified = lastModified;
		this.click = click;
		this.score = score;
	}
}