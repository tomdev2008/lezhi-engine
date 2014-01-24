package org.buzzinate.lezhi.analysis;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;

public class SnippetAnalyzerProvider extends AbstractIndexAnalyzerProvider<SnippetAnalyzer> {

    private final SnippetAnalyzer analyzer;

    @Inject
    public SnippetAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        analyzer = new SnippetAnalyzer();
    }

    @Override
    public SnippetAnalyzer get() {
        return this.analyzer;
    }
}