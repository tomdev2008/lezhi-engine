package org.buzzinate.lezhi.plugin;

import org.buzzinate.lezhi.analysis.SnippetAnalyzerProvider;
import org.buzzinate.lezhi.analysis.TermFreqFieldAnalyzerProvider;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.query.IndexQueryParserModule;
import org.elasticsearch.plugins.AbstractPlugin;

public class LezhiPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "lezhi-plugin";
    }

    @Override
    public String description() {
        return "Lezhi recommendation algorithm plugin";
    }

    public void onModule(AnalysisModule module) {
    	module.addAnalyzer("lezhi_keyword", TermFreqFieldAnalyzerProvider.class);
        module.addAnalyzer("snippet", SnippetAnalyzerProvider.class);
    }
    
    public void onModule(IndexQueryParserModule module) {
        module.addFilterParser(LezhiPrefixParser.NAME, LezhiPrefixParser.class);
    	module.addFilterParser(LezhiTimeRangeParser.NAME, LezhiTimeRangeParser.class);
    	module.addQueryParser(LezhiQueryParser.NAME, LezhiQueryParser.class);
        module.addQueryParser(MinhashQueryParser.NAME, MinhashQueryParser.class);
        module.addQueryParser(ItemQueryParser.NAME, ItemQueryParser.class);
    }
}
