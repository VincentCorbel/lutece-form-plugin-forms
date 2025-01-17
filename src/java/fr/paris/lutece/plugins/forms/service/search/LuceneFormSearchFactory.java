/*
 * Copyright (c) 2002-2014, Mairie de Paris
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

import fr.paris.lutece.portal.service.util.AppLogService;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import fr.paris.lutece.portal.service.util.AppPathService;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Factory for the search on Directory
 */
public class LuceneFormSearchFactory
{
    // Constants
    private static final String PATH_INDEX = "forms.internalIndexer.lucene.indexPath";

    // Variables
    @Inject
    @Named( value = "forms.luceneFrenchAnalizer" )
    private Analyzer _analyzer;

    /**
     * Return the Analyzer to use for the search
     * 
     * @return the Analyzer to use for the search
     */
    public Analyzer getAnalyzer( )
    {
        return _analyzer;
    }

    /**
     * Return the IndexSearcher to use for the search
     * 
     * @return the index searcher to use for the search
     * @throws IOException
     *             - if there is a low-level IO error
     */
    public IndexSearcher getIndexSearcher( ) throws IOException
    {
        Directory luceneDirectory = getDirectory( );

        return new IndexSearcher( DirectoryReader.open( luceneDirectory ) );
    }

    /**
     * Create the IndexWriter with its configuration
     * 
     * @param bCreateIndex
     *            The boolean which tell if the index must be created
     * @return the created IndexWriter
     * @throws IOException
     *             - if there is a low level IO error
     */
    public IndexWriter getIndexWriter( Boolean bCreateIndex )
    {
        try
        {
            Directory luceneDirectory = getDirectory( );

            if ( !DirectoryReader.indexExists( luceneDirectory ) )
            {
                bCreateIndex = Boolean.TRUE;
            }

            IndexWriterConfig conf = new IndexWriterConfig( getAnalyzer( ) );

            if ( bCreateIndex )
            {
                conf.setOpenMode( OpenMode.CREATE );
            }
            else
            {
                conf.setOpenMode( OpenMode.APPEND );
            }
            return new IndexWriter( luceneDirectory, conf );
        }
        catch( IOException e )
        {
            AppLogService.error( "Unable to create a new Lucene Index Writer", e );
            return null;
        }
    }

    /**
     * Return the Directory to use for the search
     * 
     * @return the Directory to use for the search
     * @throws IOException
     *             - if the path string cannot be converted to a Path
     */
    private Directory getDirectory( ) throws IOException
    {
        String strIndex = AppPathService.getPath( PATH_INDEX );

        return NIOFSDirectory.open( Paths.get( strIndex ) );
    }
}
