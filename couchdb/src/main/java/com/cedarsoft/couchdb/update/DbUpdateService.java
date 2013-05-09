package com.cedarsoft.couchdb.update;

import com.cedarsoft.couchdb.ActionFailedException;
import com.cedarsoft.couchdb.ActionResponse;
import com.cedarsoft.couchdb.CouchDatabase;
import com.cedarsoft.couchdb.CouchDoc;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.exceptions.NotFoundException;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DbUpdateService {
  @Nonnull
  public static final DocId DESIGN_DOCS_VERSION_ID = new DocId( "design_documents_version" );

  @Nonnull
  private final DesignDocumentsVersionInfoSerializer serializer;
  @Nonnull
  private final CouchDatabase db;

  public DbUpdateService( @Nonnull CouchDatabase db ) {
    this( db, new DesignDocumentsVersionInfoSerializer() );
  }

  public DbUpdateService( @Nonnull CouchDatabase db, @Nonnull DesignDocumentsVersionInfoSerializer serializer ) {
    this.serializer = serializer;
    this.db = db;
  }

  @Nonnull
  public DesignDocumentsVersionInfo queryCurrentVersionInfo() throws NotFoundException, IOException, ActionFailedException {
    try {
      return db.get( DESIGN_DOCS_VERSION_ID, serializer ).getObject();
    } catch ( ActionFailedException e ) {
      if ( e.getStatus() == 404 ) {
        throw new NotFoundException( "No document found for <" + DESIGN_DOCS_VERSION_ID + ">", e );
      }
      throw e;
    }
  }

  @Nonnull
  public ActionResponse setCurrentVersion( @Nonnull DesignDocumentsVersionInfo versionInfo ) throws ActionFailedException, IOException {
    return db.put( new CouchDoc<>( DESIGN_DOCS_VERSION_ID, versionInfo ), serializer );
  }
}
