package com.cedarsoft.couchdb;

import com.cedarsoft.CanceledException;
import org.jcouchdb.db.Database;
import org.jcouchdb.db.Server;
import org.jcouchdb.db.ServerImpl;
import org.jcouchdb.exception.CouchDBException;
import org.jcouchdb.util.CouchDBUpdater;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public abstract class CouchDbTest {
  @NotNull
  protected final URI serverURI;

  protected CouchDbTest() {
    try {
      serverURI = new URI( "http://localhost:5984" );
    } catch ( URISyntaxException e ) {
      throw new RuntimeException( e );
    }
  }

  protected CouchDatabase db;
  private Server server;

  @Before
  public void setupDb() throws IOException, URISyntaxException {
    server = new ServerImpl( serverURI.getHost(), serverURI.getPort() );
    db = createDb( getTestDbName() );

    //publish views
    publishViews();
  }

  @NotNull
  @NonNls
  protected String getTestDbName() {
    return "couch_unit_test";
  }

  @NotNull
  protected CouchDatabase createDb( @NotNull @NonNls String dbName ) {
    try {
      server.deleteDatabase( dbName );
    } catch ( CouchDBException ignore ) {
    }
    assertTrue( server.createDatabase( dbName ) );

    return new CouchDatabase( serverURI, dbName );
  }

  protected void publishViews() throws URISyntaxException, IOException {
    CouchDBUpdater updater = new CouchDBUpdater();
    updater.setCreateDatabase( false );
    updater.setDatabase( new Database( server, db.getDbName() ) );

    try {
      URL resource = getViewResource();
      assertNotNull( resource );
      File file = new File( resource.toURI() );
      File viewsDir = file.getParentFile().getParentFile();

      assertTrue( viewsDir.isDirectory() );
      updater.setDesignDocumentDir( viewsDir );

      assertEquals( 2, updater.updateDesignDocuments().size() );
    } catch ( CanceledException ignore ) {
    }
  }

  @NotNull
  protected abstract URL getViewResource() throws CanceledException;

}
