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
import java.util.HashSet;

public class ItemQueryParser implements QueryParser {
    public static final String NAME = "item_query";

    @Override
    public String[] names() {
        return new String[]{NAME, Strings.toCamelCase(NAME)};
    }

    @Override
    public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
    	XContentParser parser = parseContext.parser();

        String snippetField = "snippet";
        String keywordField = "keyword";

        String snippet = "";
        String keyword = "";
        int window = 100;

        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
                token = parser.nextToken();
                if ("snippet".equals(currentFieldName)) {
                    snippet = parser.text();
                } else if ("keyword".equals(currentFieldName)) {
                	keyword = parser.text();
                } else if ("window".equals(currentFieldName)) {
                    window = parser.intValue();
                } else if ("field.snippet".equals(currentFieldName)) {
                    snippetField = parser.text();
                } else if ("field.keyword".equals(currentFieldName)) {
                    keywordField = parser.text();
                } else throw new QueryParsingException(parseContext.index(), "[item_query] query does not support [" + currentFieldName + "]");
            }
        }

        return createQuery(snippetField, snippet, window);
    }

    public static Query createQuery(String field, String snippet, int window) {
        HashSet<String> termset = new HashSet<String>();
        BooleanQuery query = new BooleanQuery();
        for (String subsnippet: StringUtils.split(snippet, '|')) {
            String[] terms = StringUtils.split(subsnippet, ' ');
            for (int i = 0; i < terms.length; i++) terms[i] = terms[i].trim();
            for (String term: terms) termset.add(term);
            for (int i = 0; i < terms.length - 1; i ++) {
                PhraseQuery pq = new PhraseQuery();
                pq.add(new Term(field, terms[i]), 0);
                pq.add(new Term(field, terms[i+1]), 1);
                pq.setBoost(5);
                query.add(pq, BooleanClause.Occur.SHOULD);
            }
        }

        BooleanQuery fastquery = new BooleanQuery();
        for (String term: termset) {
            query.add(new TermQuery(new Term(field, term)), BooleanClause.Occur.SHOULD);
            fastquery.add(new TermQuery(new Term(field, term)), BooleanClause.Occur.SHOULD);
        }

        return SegmentQueryFilter.levelQuery(query, fastquery, window);
    }
}