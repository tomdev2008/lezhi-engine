package org.buzzinate.lezhi.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SingleTermsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.AttributeSource;
import org.buzzinate.lezhi.cache.DocFreqCache;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

/**
 * score: 
 *   log(1+t.length) * dictidf(t) * (tfq(t) + fieldboost(fq)) * idf * (tfd(t) + fieldboost(fd)) * idf * timescore
 * first part goes to query: 
 *   log(1+t.length) * dictidf(t) * (tfq(t) + fieldboost(fq)) * idf
 * second part goes to each doc: 
 *   (tfd(t) + fieldboost(fd)) * idf
 * @author Brad
 *
 */
public class LezhiQuery extends Query {
	private static ESLogger logger = Loggers.getLogger(LezhiQuery.class);
	
	protected DocFreqCache cache;
	protected String indexField;
	protected int[] fieldboost = null;
	protected TermInfo[] terms = null;
	protected String signature = null;
	protected LongFieldSource timeSrc = new LongFieldSource("lastModified");
	protected int maxTerms;
	protected long refTime;
	
  /**
   * Constructs a query matching terms that cannot be represented with a single
   * Term.
   */
  public LezhiQuery(DocFreqCache cache, String indexField, int[] fieldboost, TermInfo[] terms, String signature, long refTime, int maxTerms) {
	this.cache = cache;
	this.indexField = indexField;
	this.fieldboost = fieldboost;
    this.terms = terms;
    this.signature = signature;
    this.refTime = refTime;
    this.maxTerms = maxTerms;
    assert terms != null;
  }

  /**
   * To rewrite to a simpler form, instead return a simpler
   * enum from {@link #getTermsEnum(Terms, AttributeSource)}.  For example,
   * to rewrite to a single term, return a {@link SingleTermsEnum}
   */
  @Override
	public final Query rewrite(IndexReader reader) throws IOException {
	    DoublePriorityQueue<TermQueryDetail> topTermQueue = new DoublePriorityQueue<TermQueryDetail>(maxTerms);
		DoublePriorityQueue<TermQueryDetail> termQueue = new DoublePriorityQueue<TermQueryDetail>(maxTerms);
		List<TermQueryDetail> queryTerms = new ArrayList<TermQueryDetail>();
		int numDocs = reader.numDocs();

		for (TermInfo ti : terms) {
			Term ft = new Term(indexField, ti.getTerm());
			TermQueryDetail termDetail = new TermQueryDetail(ft, fieldboost, ti.getFreq(), ti.getField(), (float) ti.getBoost());
			int docFreq = cache.get(ti.getTerm());
			if (docFreq == -1) docFreq = reader.docFreq(ft);
			if (docFreq >= 100) {
				float idf = (float) Math.log(numDocs / (docFreq + 1.0));
				double score = termDetail.queryScore(idf) * termDetail.docFreq(ti.getFreq(), ti.getField());
				termQueue.add(score, termDetail);
				if (ti.getFreq() >= 3 || ti.getField() >= 1) topTermQueue.add(score, termDetail);
				cache.put(ti.getTerm(), docFreq);
			} else {
				if (ti.getFreq() >= 3 || ti.getField() >= 1) queryTerms.add(termDetail);
			}
		}

		List<TermQueryDetail> topTerms = topTermQueue.values();
		if (topTerms.size() * 2 < maxTerms) topTerms = termQueue.values();
		queryTerms.addAll(topTerms);
		if (logger.isDebugEnabled()) logger.debug(queryTerms.toString());

		BooleanQuery q = new BooleanQuery();
		for (TermQueryDetail term : queryTerms) {
			q.add(new LezhiTermQuery(term), Occur.SHOULD);
		}
		if (signature != null) q.add(new TermQuery(new Term("signature", signature)), Occur.MUST_NOT);
		return new RecencyBoostQuery(q, refTime, new LongFieldSource("lastModified"));
	}
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(getBoost());
    for (TermInfo term: terms) result = prime * result + term.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LezhiQuery other = (LezhiQuery) obj;
    if (Float.floatToIntBits(getBoost()) != Float.floatToIntBits(other.getBoost()))
      return false;
    return Arrays.equals(terms, other.terms);
  }

  @Override
  public String toString(String field) {
	return "LezhiQuery[" + field + ":" + Arrays.toString(terms) + "]";
  }
}