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

package com.cedarsoft.couchdb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.junit.*;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class URITest {
  @Test
  public void testUserPass() throws Exception {
    URI uri = new URI( "http://user:pass@couchdb.cedarsoft.com:5984" );
    assertEquals( "couchdb.cedarsoft.com", uri.getHost() );
    assertEquals( "user:pass@couchdb.cedarsoft.com:5984", uri.getAuthority() );
    assertEquals( "user:pass", uri.getUserInfo() );
  }

  @Test
  public void testCons() throws URISyntaxException {
    WebResource resource1 = new Client().resource( "http://" + "localhost" + ":" + 8080 ).path( "daPath" );
    WebResource resource2 = new Client().resource( new URI( "http://localhost:8080/daPath" ) );

    assertEquals( resource1, resource2 );
  }

  @Test
  public void testConstructors() throws Exception {
    assertEquals( "http://localhost:8080/dbName$asdf", new CouchDatabase( new URI( "http://localhost:8080/dbName$asdf" ) ).getURI().toString() );
    assertEquals( "http://localhost:8080/dbName$asdf", new CouchDatabase( new URI( "http://localhost:8080" ), "dbName$asdf" ).getURI().toString() );
  }

  @Test
  public void testDbNameUri() throws Exception {
    URI serverUri = new URI( "http://localhost:8080" );
    String dbName = "daDb";

    assertEquals( "localhost:8080", serverUri.getAuthority() );
    assertEquals( "localhost", serverUri.getHost() );
    assertEquals( "", serverUri.getPath() );
    assertEquals( null, serverUri.getFragment() );
    assertEquals( 8080, serverUri.getPort() );
    assertEquals( "", serverUri.getRawPath() );
    assertEquals( "http", serverUri.getScheme() );
    assertEquals( null, serverUri.getUserInfo() );

    assertEquals( "http://localhost:8080/daDb", serverUri.resolve( "/" + dbName ).toString() );
  }

  @Test
  public void testUri() throws Exception {
    URI serverUri = new URI( "http://localhost:8080/daDb" );
    assertEquals( "localhost:8080", serverUri.getAuthority() );
    assertEquals( "localhost", serverUri.getHost() );
    assertEquals( "/daDb", serverUri.getPath() );
    assertEquals( null, serverUri.getFragment() );
    assertEquals( 8080, serverUri.getPort() );
    assertEquals( "/daDb", serverUri.getRawPath() );
    assertEquals( "http", serverUri.getScheme() );
    assertEquals( null, serverUri.getUserInfo() );
  }
}
