package org.buzzinate.lezhi.plugin;

import java.io.IOException;

import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.NumericRangeFilter;
import org.buzzinate.lezhi.filter.FuzzyDuplicateFilter;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.cache.filter.support.CacheKeyFilter;
import org.elasticsearch.index.query.FilterParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParsingException;

public class LezhiTimeRangeParser implements FilterParser {
	private static final int ONE_DAY = 1000 * 3600 * 24;

    public static final String NAME = "lezhi_timerange";

    public LezhiTimeRangeParser() {
    }

    @Override
    public String[] names() {
        return new String[]{NAME};
    }

    @Override
    public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        boolean cache = true;
        long mintime = -1;
        
        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
            	if ("_cache".equals(currentFieldName)) {
                    cache = parser.booleanValue();
                } else if ("mintime".equals(currentFieldName)) {
                	mintime = Long.parseLong(parser.text());
                } else throw new QueryParsingException(parseContext.index(), "[lezhi_timerange] filter does not support [" + currentFieldName + "]");
            }
        }

        Filter filter = null;
        String cacheKey = null;
        if (mintime == -1) {
        	filter = new FuzzyDuplicateFilter("signature");
        	cacheKey = parseContext.index().name();
        } else {
        	BooleanFilter filters = new BooleanFilter();
        	filters.add(NumericRangeFilter.newLongRange("lastModified", mintime, System.currentTimeMillis(), true, true), Occur.MUST);
        	filters.add(new FuzzyDuplicateFilter("signature"), Occur.MUST);
        	filter = filters;
        	cacheKey = parseContext.index().name() + "-" + String.valueOf(mintime / ONE_DAY * ONE_DAY);
        }

        if (cache) {
            filter = parseContext.cacheFilter(filter, new CacheKeyFilter.Key(cacheKey));
        }

        return filter;
    }
}