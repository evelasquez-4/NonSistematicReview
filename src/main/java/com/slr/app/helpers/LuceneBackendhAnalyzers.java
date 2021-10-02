package com.slr.app.helpers;

import org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.en.PorterStemFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.miscellaneous.KeywordRepeatFilterFactory;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilterFactory;
import org.apache.lucene.analysis.miscellaneous.TrimFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;

import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;

public class LuceneBackendhAnalyzers implements LuceneAnalysisConfigurer{

	@Override
	public void configure(LuceneAnalysisConfigurationContext context) {
		context.analyzer( "english_analyzer" ).custom() 
        .tokenizer( StandardTokenizerFactory.class )
        .charFilter( HTMLStripCharFilterFactory.class )
        .tokenFilter( LowerCaseFilterFactory.class ) 
        .tokenFilter( SnowballPorterFilterFactory.class ) 
                .param( "language", "English" )
        .tokenFilter (TrimFilterFactory.class)
        .tokenFilter (KeywordRepeatFilterFactory.class)
        .tokenFilter (PorterStemFilterFactory.class)
        .tokenFilter (RemoveDuplicatesTokenFilterFactory.class)
        .tokenFilter( ASCIIFoldingFilterFactory.class );		

		
		
		context.normalizer("english_normalyzer")
			.custom()
			.tokenFilter(LowerCaseFilterFactory.class)
			.tokenFilter(ASCIIFoldingFilterFactory.class);
		
		context.analyzer("simple_field_analyzer").custom()
		.tokenizer( StandardTokenizerFactory.class )
        .tokenFilter( LowerCaseFilterFactory.class )
        .tokenFilter( ASCIIFoldingFilterFactory.class )
        .tokenFilter( SnowballPorterFilterFactory.class ) 
        .param( "language", "English" );
	}

}
