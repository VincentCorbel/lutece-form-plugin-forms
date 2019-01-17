/*
 * Copyright (c) 2002-2018, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.forms.service.search;

import fr.paris.lutece.plugins.forms.business.form.search.FormResponseSearchItem;
import fr.paris.lutece.portal.service.search.IndexationService;
import fr.paris.lutece.portal.service.search.LuceneSearchEngine;
import fr.paris.lutece.portal.service.search.SearchItem;
import fr.paris.lutece.portal.service.util.AppLogService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;


public class LuceneFormSearchEngine implements IFormSearchEngine
{

    @Inject
    private LuceneFormSearchFactory _luceneFormSearchFactory;
    
    @Override
    public List<Integer> getSearchResults( FormSearchConfig formSearchConfig ) 
    {
        ArrayList<Integer> listResults = new ArrayList<>( );
        IndexSearcher searcher = null;

        try
        {
            searcher = _luceneFormSearchFactory.getIndexSearcher( );

            Collection<String> queries = new ArrayList<>( );
            Collection<String> fields = new ArrayList<>( );
            Collection<BooleanClause.Occur> flags = new ArrayList<>( );

            Query queryContent = new TermQuery( new Term( FormResponseSearchItem.FIELD_CONTENTS, formSearchConfig.getSearchedText( ) ) );
            queries.add( queryContent.toString( ) );
            fields.add( FormResponseSearchItem.FIELD_CONTENTS );
            flags.add( BooleanClause.Occur.MUST );

            Query queryMulti = MultiFieldQueryParser.parse( queries.toArray( new String [ queries.size( )] ), fields.toArray( new String [ fields.size( )] ),
                    flags.toArray( new BooleanClause.Occur [ flags.size( )] ), IndexationService.getAnalyser( ) );

            // Get results documents
            TopDocs topDocs = searcher.search( queryMulti, LuceneSearchEngine.MAX_RESPONSES );
            ScoreDoc [ ] hits = topDocs.scoreDocs;

            for ( int i = 0; i < hits.length; i++ )
            {
                Document document = searcher.doc( hits [i].doc  );
                SearchItem si = new SearchItem( document );
                listResults.add( Integer.parseInt( si.getId() ));
            }
        }
        catch( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
        }

        return listResults;
    }
    
}
