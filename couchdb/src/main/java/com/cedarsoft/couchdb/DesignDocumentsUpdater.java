package com.cedarsoft.couchdb;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Updates the design documents
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DesignDocumentsUpdater {
  @Nonnull
  private final CouchDatabase database;

  public DesignDocumentsUpdater( @Nonnull CouchDatabase database ) {
    this.database = database;
  }

  public void update( @Nonnull Iterable<? extends DesignDocument> designDocuments ) throws IOException, ActionFailedException {
    for ( DesignDocument designDocument : designDocuments ) {
      if ( !designDocument.hasViews( ) ) {
        continue;
      }

      String path = designDocument.getDesignDocumentPath( );
      WebResource resource = database.getDbRoot( ).path( path );

      ClientResponse response = resource.put( ClientResponse.class, designDocument.createJson( ) );
      ActionResponse.verifyNoError( response );
    }
  }
}
