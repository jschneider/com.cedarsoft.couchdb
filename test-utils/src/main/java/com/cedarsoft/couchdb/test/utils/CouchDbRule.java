/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *         http://www.cedarsoft.org/gpl3ce
 *         (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
 */

package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.CouchDatabase;
import com.cedarsoft.couchdb.CouchDbException;
import com.cedarsoft.exceptions.CanceledException;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.security.auth.UserPrincipal;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.jcouchdb.db.Database;
import org.jcouchdb.db.Server;
import org.jcouchdb.db.ServerImpl;
import org.jcouchdb.exception.CouchDBException;
import org.jcouchdb.util.CouchDBUpdater;
import org.junit.rules.*;
import org.junit.runners.model.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchDbRule implements MethodRule {

  public static final String KEY_DB_NAME = "couchdb.unittests.db.name";

  public static final String KEY_SERVER_URI = "couchdb.unittests.server.uri";

  public static final String KEY_SKIP_DELETE_DB = "couchdb.unittests.skip.deletion";


  public static final String KEY_USER = "couchdb.unittests.username";

  public static final String KEY_PASS = "couchdb.unittests.password";

  @Nullable
  protected CouchDatabase db;

  /**
   * Contains all dbs that are currently active
   */
  private final Set<CouchDatabase> dbs = new HashSet<CouchDatabase>();

  @Nullable
  protected URI serverURI;
  @Nullable
  private Server server;
  @Nullable
  private final URL viewResource;
  @Nullable

  private final String dbBaseName;


  public CouchDbRule() {
    this( null );
  }

  /**
   * The view resource will be used to detect the views that are copied to the server
   *
   * @param viewResource the optional view resource
   */
  public CouchDbRule( @Nullable URL viewResource ) {
    this( viewResource, null );
  }

  public CouchDbRule( @Nullable URL viewResource, @Nullable String dbBaseName ) {
    this.viewResource = viewResource;
    this.dbBaseName = dbBaseName;
  }

  public void before() throws IOException, URISyntaxException, CouchDbException {
    URI currentUri = getServerUri();
    serverURI = currentUri;
    Server currentServer = new ServerImpl( serverURI.getHost(), currentUri.getPort() );

    final String username = getUsername();
    final String password = getPassword();

    if ( username != null && password != null ) {
      currentServer.setCredentials( AuthScope.ANY, new Credentials() {
        @Override
        public Principal getUserPrincipal() {
          return new UserPrincipal( username );
        }

        @Override
        public String getPassword() {
          return password;
        }
      } );
    }

    this.server = currentServer;
    db = createDb( createNewTestDbName() );
  }

  @Nullable
  protected String getUsername() {
    return System.getProperty( KEY_USER );
  }

  @Nullable
  protected String getPassword() {
    return System.getProperty( KEY_PASS );
  }

  public void after() {
    deleteDatabases();
  }

  protected void deleteDatabases() {
    if ( Boolean.parseBoolean( System.getProperty( KEY_SKIP_DELETE_DB ) ) ) {
      System.out.println( "----------------------------" );
      System.out.println( "Skipping deletion of " + db.getDbName() );
      System.out.println( "----------------------------" );
      return;
    }

    Server currentServer = server;
    if ( currentServer != null ) {
      for ( Iterator<CouchDatabase> iterator = dbs.iterator(); iterator.hasNext(); ) {
        CouchDatabase couchDatabase = iterator.next();
        currentServer.deleteDatabase( couchDatabase.getDbName() );
        iterator.remove();
      }
    }
    db = null;
  }

  @Nonnull
  public CouchDatabase createDb( @Nonnull String dbName ) throws IOException, URISyntaxException, CouchDbException {
    try {
      server.deleteDatabase( dbName );
    } catch ( CouchDBException ignore ) {
    }

    assertTrue( server.createDatabase( dbName ) );
    publishViews( dbName );

    CouchDatabase couchDatabase = getCouchDatabaseObject( dbName );

    this.dbs.add( couchDatabase );
    return couchDatabase;
  }

  public void deleteDb( @Nonnull String dbName ) {
    Server currentServer = server;
    if ( currentServer == null ) {
      throw new IllegalArgumentException( "Invalid state - server is null" );
    }
    currentServer.deleteDatabase( dbName );
  }

  /**
   * Creates  a new database object - but does *not* create anything on the server
   *
   * @param dbName the db name
   * @return the couch database object
   */
  @Nonnull
  public CouchDatabase getCouchDatabaseObject( @Nonnull String dbName ) {
    URI uri = serverURI;
    assert uri != null;

    ClientFilter[] filters;
    @Nullable String username = getUsername();
    @Nullable String password = getPassword();
    if ( username != null && password != null ) {
      filters = new ClientFilter[]{new HTTPBasicAuthFilter( username, password )};
    }else{
      filters = new ClientFilter[0];
    }

    return new CouchDatabase( uri, dbName, filters );
  }

  public void publishViews( @Nonnull String dbName ) throws URISyntaxException, IOException {
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

  @Nonnull

  public String createNewTestDbName() {
    return getTestDbBaseName() + System.currentTimeMillis();
  }


  @Nonnull

  public String getTestDbBaseName() {
    if ( dbBaseName != null ) {
      return dbBaseName;
    }
    return System.getProperty( KEY_DB_NAME, "couch_unit_test" );
  }

  @Nonnull
  public URI getServerUri() throws URISyntaxException {
    return new URI( System.getProperty( KEY_SERVER_URI, "http://localhost:5984" ) );
  }

  @Nonnull
  public CouchDatabase getCurrentDb() {
    if ( db == null ) {
      throw new IllegalStateException( "No db available" );
    }
    return db;
  }

  @Nonnull
  public URI getCurrentServerURI() {
    if ( serverURI == null ) {
      throw new IllegalStateException( "No server uri" );
    }
    return serverURI;
  }

  @Nonnull
  public Server getCurrentServer() {
    if ( server == null ) {
      throw new IllegalStateException( "No server " );
    }
    return server;
  }
}
