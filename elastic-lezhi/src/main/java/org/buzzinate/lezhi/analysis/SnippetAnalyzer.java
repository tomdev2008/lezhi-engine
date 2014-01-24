package org.buzzinate.lezhi.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;

public class SnippetAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final Tokenizer source = new WhitespaceTokenizer(Version.LUCENE_43, reader);
        return new TokenStreamComponents(source, new SnippetTokenFilter(source, SnippetTokenFilter.DEFAULT_DELIMITER));
    }

    public static final class SnippetTokenFilter extends FilteringTokenFilter {
        public static final char DEFAULT_DELIMITER = '|';
        private final char delimiter;
        private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

        public SnippetTokenFilter(TokenStream input, char delimiter) {
            super(true, input);
            this.delimiter = delimiter;
        }

        @Override
        public  boolean accept() throws IOException {
            return termAtt.length() > 1 || termAtt.buffer()[0] != delimiter;
        }
    }
}