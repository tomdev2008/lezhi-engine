package org.buzzinate.lezhi.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.buzzinate.lezhi.analysis.SnippetAnalyzer;

import java.io.IOException;
import java.io.StringReader;

public class TestAnalyzer {
    public static void main(String[] args) throws IOException {
        Analyzer analyzer = new SnippetAnalyzer();
        TokenStream ts = analyzer.tokenStream("text", new StringReader("中新网 社区 | 中国 媒体 | 朝鲜 赌注 | 高 | 中国 | 适当 惩罚"));
        ts.reset();
        while (ts.incrementToken()) {
//            System.out.println(StringUtils.join(ts.getAttributeClassesIterator(), ""));
            CharTermAttribute ct = ts.getAttribute(CharTermAttribute.class);
            PositionIncrementAttribute pt = ts.getAttribute(PositionIncrementAttribute.class);
            System.out.println(ct.toString() + " => " + pt.getPositionIncrement());
        }
    }
}
