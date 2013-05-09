package com.cedarsoft.couchdb.update;

import com.cedarsoft.couchdb.ActionFailedException;
import com.cedarsoft.couchdb.CouchDatabase;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.exceptions.NotFoundException;
import com.cedarsoft.version.Version;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DbUpdateService {
  @Nonnull
  public static final DocId DESIGN_DOCS_VERSION_ID = new DocId( "design_documents_version" );
  public static final Charset CHARSET = Charsets.UTF_8;

  @Nonnull
  private final CouchDatabase db;

  public DbUpdateService( @Nonnull CouchDatabase db ) {
    this.db = db;
  }

  @Nonnull
  public Version queryCurrentVersion() throws NotFoundException, IOException, ActionFailedException {
    try {
      byte[] bytes = ByteStreams.toByteArray( db.get( DESIGN_DOCS_VERSION_ID ) );
      return Version.parse( new String( bytes, CHARSET ) );
    } catch ( ActionFailedException e ) {
      if ( e.getStatus() == 404 ) {
        throw new NotFoundException( "No document found for <" + DESIGN_DOCS_VERSION_ID + ">", e );
      }
      throw e;
    }
  }

  public void setCurrentVersion( @Nonnull Version version ) throws ActionFailedException {
    db.put( DESIGN_DOCS_VERSION_ID, new ByteArrayInputStream( version.toString().getBytes( CHARSET ) ) );
  }
}
