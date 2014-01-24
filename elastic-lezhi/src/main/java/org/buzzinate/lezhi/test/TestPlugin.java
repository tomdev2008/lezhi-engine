package org.buzzinate.lezhi.test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.SimpleFSDirectory;
import org.buzzinate.lezhi.cache.ElasticDocFreqCache;
import org.buzzinate.lezhi.filter.FuzzyDuplicateFilter;
import org.buzzinate.lezhi.query.LezhiQuery;
import org.buzzinate.lezhi.query.TermInfo;
import org.buzzinate.lezhi.query.TermQueryDetail;
import org.elasticsearch.common.cache.Cache;
import org.elasticsearch.common.cache.CacheBuilder;

public class TestPlugin {
	public static void main(String[] args) throws IOException {
		DirectoryReader reader = DirectoryReader.open(new SimpleFSDirectory(new File("D:/data/chinadaily/index")));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Cache<String, Integer> cache = CacheBuilder.newBuilder()
			.maximumSize(10000 * 100)
			.expireAfterWrite(10, TimeUnit.MINUTES).expireAfterAccess(1, TimeUnit.MINUTES)
			.build();
		
//		String url = "http://cyol.net/yuqing/content/2013-01/16/content_7741014.htm";
		String siteprefix = "http://www.chinadaily.com.cn";
		BooleanFilter filters = new BooleanFilter();
//		filters.add(NumericRangeFilter.newLongRange("lastModified", 1355760000000L, System.currentTimeMillis(), true, true), Occur.MUST);
		filters.add(new PrefixFilter(new Term("url", siteprefix)), Occur.MUST);
		filters.add(new FuzzyDuplicateFilter("signature"), Occur.MUST);
		Filter filter = new CachingWrapperFilter(filters);
		
    	TermInfo[] tis = TermInfo.parse("身材|4,0,0.6919598325487599 齐刘海|3,2,0.9361324551106742 引人注目|3,0,0.14666500252244027 婆婆装|5,2,0.7310125935176603 合体|5,2,0.8000000000000002 婆婆|5,2,0.6919598325487599 大小|8,2,0.4375687958994869 大小s|8,2,0.9187905133244337 大s|9,3,0.8381252177893271 麒麟|4,0,0.7534696325484268 大小s合体|5,2,0.9350324106595469 怀孕|6,2,0.20000000000000004 妈妈|6,0,0.5119851888145414 青春|8,2,0.573956359223955 麒麟臂|4,0,0.8629962075867058 小s|17,2,0.8664811927033865 刘海|2,2,1.0");
    	LezhiQuery query = new LezhiQuery(new ElasticDocFreqCache(cache, "chinadaily"), "keyword", TermQueryDetail.DEFAULT_FIELD_BOOST, tis, "d47bcc623e30a71e", 1355760000000L, 5);
//    	Query q = query.rewrite(reader);
//    	
//    	for (int i = 0; i < Integer.MAX_VALUE; i++) {
//    		long start = System.currentTimeMillis();
//    		searcher.search(query, filter, 20);
//    		System.out.println("cost(ms): " + (System.currentTimeMillis() - start));
//    	}
    	
    	TopDocs result = searcher.search(query, filter, 20);
    	System.out.println("total: " + result.totalHits);
    	for (ScoreDoc doc: result.scoreDocs) {
    		Document d = searcher.doc(doc.doc);
    		System.out.println(doc.score + " => " + d.get("title") + " / " + d.get("url") + " => " + d.get("signature"));
//    		System.out.println(searcher.explain(query, doc.doc));
    	}
    	reader.close();
	}
}