package com.buzzinate.json;

public class RenderThumbnail {
	public String url;
	public String thumbnail;
	public double score;
	
	public RenderThumbnail() {
	}

    public RenderThumbnail(String url, String thumbnail, double score) {
        this.url = url;
        this.thumbnail = thumbnail;
        this.score = score;
    }
}