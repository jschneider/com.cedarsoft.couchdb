package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.DesignDocument;
import com.cedarsoft.couchdb.DesignDocuments;
import com.cedarsoft.couchdb.DesignDocumentsUpdater;
import com.cedarsoft.test.utils.JsonUtils;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.*;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DesignDocumentsUpdaterTest extends CouchTest {
  @Test
  public void testIt( ) throws Exception {
    assertThat( db( ) ).isNotNull( );

    List<? extends DesignDocument> designDocuments = DesignDocuments.createDesignDocuments( getClass( ).getResource( "views/doc1/file1.map.js" ) );
    assertThat( designDocuments ).hasSize( 3 );

    DesignDocumentsUpdater updater = new DesignDocumentsUpdater( db( ) );
    updater.update( designDocuments );

    {
      ClientResponse response = db( ).getDbRoot( ).path( "_design/doc1" ).get( ClientResponse.class );
      assertThat( response ).isNotNull( );
      assertThat( response.getStatus( ) ).isEqualTo( 200 );
    }
    {
      ClientResponse response = db( ).getDbRoot( ).path( "_design/doc1/_view/file1" ).get( ClientResponse.class );
      assertThat( response ).isNotNull( );
      assertThat( response.getStatus( ) ).isEqualTo( 200 );
    }
  }
}
