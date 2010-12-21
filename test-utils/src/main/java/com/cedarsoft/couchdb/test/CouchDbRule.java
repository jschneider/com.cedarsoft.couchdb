package com.cedarsoft.couchdb.test;

import com.cedarsoft.CanceledException;
import com.cedarsoft.couchdb.CouchDatabase;
import org.jcouchdb.db.Database;
import org.jcouchdb.db.Server;
import org.jcouchdb.db.ServerImpl;
import org.jcouchdb.exception.CouchDBException;
import org.jcouchdb.util.CouchDBUpdater;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.rules.*;
import org.junit.runners.model.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchDbRule implements MethodRule {
  @NonNls
  public static final String KEY_DB_NAME = "couchdb.unittests.db.name";
  @NonNls
  public static final String KEY_SERVER_URI = "couchdb.unittests.server.uri";
  @NonNls
  public static final String KEY_SKIP_DELETE_DB = "couchdb.unittests.skip.deletion";

  @Nullable
  protected CouchDatabase db;
  @Nullable
  protected URI serverURI;
  @Nullable
  private Server server;
  @Nullable
  private final URL viewResource;

  public CouchDbRule() {
    this( null );
  }

  /**
   * The view resource will be used to detect the views that are copied to the server
   *
   * @param viewResource the optional view resource
   */
  public CouchDbRule( @Nullable URL viewResource ) {
    this.viewResource = viewResource;
  }

  public void before() throws IOException, URISyntaxException {
    serverURI = getServerUri();
    server = new ServerImpl( serverURI.getHost(), serverURI.getPort() );
    db = createDb( createNewTestDbName() );
  }

  public void after() {
    deleteDatabase();
  }

  private void deleteDatabase() {
    if ( Boolean.parseBoolean( System.getProperty( KEY_SKIP_DELETE_DB ) ) ) {
      System.out.println( "----------------------------" );
      System.out.println( "Skipping deletion of " + db.getDbName() );
      System.out.println( "----------------------------" );
      return;
    }

    Server currentServer = server;
    if ( currentServer != null ) {
      currentServer.deleteDatabase( db.getDbName() );
    }
    db = null;
  }

  @NotNull
  public CouchDatabase createDb( @NotNull @NonNls String dbName ) throws IOException, URISyntaxException {
    try {
      server.deleteDatabase( dbName );
    } catch ( CouchDBException ignore ) {
    }
    assertTrue( server.createDatabase( dbName ) );

    publishViews( dbName );

    return new CouchDatabase( serverURI, dbName );
  }

  public void publishViews( @NotNull @NonNls String dbName ) throws URISyntaxException, IOException {
    CouchDBUpdater updater = new CouchDBUpdater();
    updater.setCreateDatabase( false );
    updater.setDatabase( new Database( server, dbName ) );

    try {
      URL resource = getViewResource();
      if ( resource == null ) {
        return;
      }
      File file = new File( resource.toURI() );
      File viewsDir = file.getParentFile().getParentFile();

      assertTrue( viewsDir.isDirectory() );
      updater.setDesignDocumentDir( viewsDir );

      updater.updateDesignDocuments();
    } catch ( CanceledException ignore ) {
    }
  }

  /**
   * Returns one view resource that is used to find the base dir for all views
   *
   * @return one view resource
   */
  @Nullable
  public URL getViewResource() {
    return viewResource;
  }

  @Override
  public Statement apply( final Statement base, FrameworkMethod method, Object target ) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        before();
        try {
          base.evaluate();
        } finally {
          after();
        }
      }
    };
  }

  @NotNull
  @NonNls
  public String createNewTestDbName() {
    return getTestDbBaseName() + System.currentTimeMillis();
  }

  @NotNull
  @NonNls
  public String getTestDbBaseName() {
    return System.getProperty( KEY_DB_NAME, "couch_unit_test" );
  }

  @NotNull
  public URI getServerUri() throws URISyntaxException {
    return new URI( System.getProperty( KEY_SERVER_URI, "http://localhost:5984" ) );
  }

  @NotNull
  public CouchDatabase getDb() {
    if ( db == null ) {
      throw new IllegalStateException( "Not db" );
    }
    return db;
  }

  @NotNull
  public URI getServerURI() {
    if ( serverURI == null ) {
      throw new IllegalStateException( "No server uri" );
    }
    return serverURI;
  }

  @NotNull
  public Server getServer() {
    if ( server == null ) {
      throw new IllegalStateException( "No server " );
    }
    return server;
  }
}
