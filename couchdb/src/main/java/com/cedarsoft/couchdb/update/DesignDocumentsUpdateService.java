package com.cedarsoft.couchdb.update;

import com.cedarsoft.couchdb.core.ActionFailedException;
import com.cedarsoft.couchdb.ActionResponse;
import com.cedarsoft.couchdb.CouchDatabase;
import com.cedarsoft.couchdb.core.CouchDoc;
import com.cedarsoft.couchdb.DesignDocument;
import com.cedarsoft.couchdb.DesignDocumentsProvider;
import com.cedarsoft.couchdb.DesignDocumentsUpdater;
import com.cedarsoft.couchdb.core.DocId;
import com.cedarsoft.couchdb.core.Revision;
import com.cedarsoft.exceptions.NotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DesignDocumentsUpdateService {
  @Nonnull
  public static final DocId DESIGN_DOCS_VERSION_ID = new DocId( "design_documents_version" );

  private static final Logger log = Logger.getLogger( DesignDocumentsUpdateService.class.getName() );

  @Nonnull
  private final DesignDocumentsVersionInfoSerializer serializer;
  @Nonnull
  private final CouchDatabase db;

  public DesignDocumentsUpdateService( @Nonnull CouchDatabase db ) {
    this( db, new DesignDocumentsVersionInfoSerializer() );
  }

  public DesignDocumentsUpdateService( @Nonnull CouchDatabase db, @Nonnull DesignDocumentsVersionInfoSerializer serializer ) {
    this.serializer = serializer;
    this.db = db;
  }

  @Nonnull
  public DesignDocumentsVersionInfo queryCurrentVersionInfo() throws NotFoundException, IOException, ActionFailedException {
    return queryCurrentVersionInfoDoc().getObject();
  }

  @Nonnull
  private CouchDoc<DesignDocumentsVersionInfo> queryCurrentVersionInfoDoc() throws ActionFailedException, NotFoundException {
    try {
      return db.get( DESIGN_DOCS_VERSION_ID, serializer );
    } catch ( ActionFailedException e ) {
      if ( e.getStatus() == 404 ) {
        throw new NotFoundException( "No document found for <" + DESIGN_DOCS_VERSION_ID + ">", e );
      }
      throw e;
    }
  }

  @Nonnull
  public ActionResponse setCurrentVersion( @Nonnull DesignDocumentsVersionInfo versionInfo, @Nullable Revision oldRevision ) throws ActionFailedException, IOException {
    return db.put( new CouchDoc<>( DESIGN_DOCS_VERSION_ID, oldRevision, versionInfo ), serializer );
  }

  /**
   * Returns null if nothing has been done
   *
   * @param provider the provider
   * @return the updated version info or null if no updated has been done
   *
   * @throws IOException
   * @throws ActionFailedException
   */
  @Nullable
  public DesignDocumentsVersionInfo updateIfNecessary( @Nonnull DesignDocumentsProvider provider ) throws IOException, ActionFailedException {
    Revision rev = null;
    try {
      CouchDoc<DesignDocumentsVersionInfo> doc = queryCurrentVersionInfoDoc();
      rev = doc.getRev();
      DesignDocumentsVersionInfo currentVersionInfo = doc.getObject();

      if ( currentVersionInfo.getVersion().sameOrGreaterThan( provider.getVersion() ) ) {
        log.info( "Currently installed version <" + currentVersionInfo.getVersion() + ">. No need to update to <" + provider.getVersion() + ">" );
        return null;
      }
    } catch ( NotFoundException ignore ) {
      log.info( "No DesignDocumentsVersionInfo found" );
    }

    //is necessary?
    log.info( "Upgrading design documents to <" + provider.getVersion() + ">" );
    publishDesignDocuments( provider.getDesignDocuments() );

    DesignDocumentsVersionInfo versionInfo = new DesignDocumentsVersionInfo( provider.getVersion(), System.currentTimeMillis(), createDescriptionString() );
    setCurrentVersion( versionInfo, rev );
    return versionInfo;
  }

  @Nonnull
  public static String createDescriptionString() {
    String hostName;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch ( UnknownHostException ignore ) {
      hostName = "unknown";
    }
    return System.getProperty( "user.name" ) + "@" + hostName;
  }

  private void publishDesignDocuments( @Nonnull Iterable<? extends DesignDocument> designDocuments ) throws IOException, ActionFailedException {
    DesignDocumentsUpdater updater = new DesignDocumentsUpdater( db );
    updater.update( designDocuments );
  }
}
