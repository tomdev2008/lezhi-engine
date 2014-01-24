package org.buzzinate.lezhi.plugin;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.buzzinate.lezhi.query.SegmentQueryFilter;
import org.buzzinate.lezhi.util.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParser;
import org.elasticsearch.index.query.QueryParsingException;

import java.io.IOException;

public class MinhashQueryParser implements QueryParser {
    public static final String NAME = "minhash_query";

    @Override
    public String[] names() {
        return new String[]{NAME, Strings.toCamelCase(NAME)};
    }

    @Override
    public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
    	XContentParser parser = parseContext.parser();

        String bucketField = "bucket";
        String minhashField = "minhash";
        String buckets = "";
        String minhashes = "";
        int groupSize = 5;
        int topn = 40;

        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
                token = parser.nextToken();
                if ("field.bucket".equals(currentFieldName)) {
                    bucketField = parser.text();
                } else if ("field.minhash".equals(currentFieldName)) {
                	minhashField = parser.text();
                } else if ("groupSize".equals(currentFieldName)) {
                    groupSize = parser.intValue();
                } else if ("bucket".equals(currentFieldName)) {
                    buckets = parser.text();
                } else if ("minhash".equals(currentFieldName)) {
                    minhashes = parser.text();
                } else if ("topn".equals(currentFieldName)) {
                    topn = parser.intValue();
                } else throw new QueryParsingException(parseContext.index(), "[minhash_query] query does not support [" + currentFieldName + "]");
            }
        }

        BooleanQuery bq = new BooleanQuery();
        for (String bucket: StringUtils.split(buckets, ' ')) bq.add(new TermQuery(new Term(bucketField, bucket)), BooleanClause.Occur.SHOULD);

        BooleanQuery mq = new BooleanQuery();
        for (String minhash: StringUtils.split(minhashes, ' ')) {
            mq.add(new TermQuery(new Term(minhashField, minhash)), BooleanClause.Occur.SHOULD);
        }

        return SegmentQueryFilter.levelQuery(mq, bq, topn * 2);
    }
}