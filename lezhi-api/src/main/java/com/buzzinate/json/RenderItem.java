package com.buzzinate.json;

import com.buzzinate.lezhi.thrift.PicType;

public class RenderItem {
	public String url; // required
	public String title; // required
	public String pic; // optional
	public PicType picType; // optional
	public double score; // optional
	public double hotScore; // optional

	public RenderItem() {
	}
}