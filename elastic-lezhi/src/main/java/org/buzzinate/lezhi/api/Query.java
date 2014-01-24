package org.buzzinate.lezhi.api;

public class Query {
	public String signature;
	public String keyword;
	public long reftime;
	
	public Query() {}
	
	public Query(String signature, String keyword, long reftime) {
		this.signature = signature;
		this.keyword = keyword;
		this.reftime = reftime;
	}
}