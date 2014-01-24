package org.buzzinate.lezhi.query;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.FixedBitSet;

import java.io.IOException;

public class SegmentQueryFilter extends Filter {
    private Query query;
    private int window;

    public SegmentQueryFilter(Query query, int window) {
        this.query = query;
        this.window = window;
    }

    public static Query levelQuery(Query query, Query fast, int window) {
        return new FilteredQuery(query, new SegmentQueryFilter(fast, window));
    }

    @Override
    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
        AtomicReader reader = context.reader();
        FixedBitSet bits = new FixedBitSet(reader.maxDoc());

        AtomicReaderContext ctx = reader.getContext();
        int limit = reader.maxDoc();
        if (limit == 0) limit = 1;
        int nDocs = Math.min(window, limit);
        Weight weight = new IndexSearcher(reader).createNormalizedWeight(query);
        TopScoreDocCollector collector = TopScoreDocCollector.create(nDocs, null, !weight.scoresDocsOutOfOrder());
        collector.setNextReader(ctx);
        Scorer scorer = weight.scorer(ctx, !collector.acceptsDocsOutOfOrder(), true, acceptDocs);
        if (scorer != null) scorer.score(collector);
        for (ScoreDoc sd : collector.topDocs().scoreDocs) {
            bits.set(sd.doc);
        }

        return bits;
    }

    @Override
    public String toString() {
        return "SegmentQueryFilter(q=" + query.toString() + ", window=" + window + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SegmentQueryFilter)) return false;
        SegmentQueryFilter other = (SegmentQueryFilter) o;
        return query.equals(other.query) && window == other.window;
    }

    @Override
    public int hashCode() {
        return query.hashCode();
    }
}