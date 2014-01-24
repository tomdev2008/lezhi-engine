package com.buzzinate.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buzzinate.lezhi.thrift.RecommendType;

public class RecResult {
	public Map<RecommendType, List<RecItem>> result = new HashMap<RecommendType, List<RecItem>>();
	// info
	public int count;
	public Set<RecommendType> recTypes = new HashSet<RecommendType>();
	public long recommTime;
	public long numDocs;
	
	public RecResult() {
	}

	// TODO: fastjson requires long field has setter
	public void setNumDocs(long numDocs) {
		this.numDocs = numDocs;
	}

	public void setRecommTime(long recommTime) {
		this.recommTime = recommTime;
	}
}