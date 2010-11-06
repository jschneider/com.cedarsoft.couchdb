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

package com.cedarsoft.couchdb.naming;

import com.sun.jersey.api.uri.UriBuilderImpl;
import org.junit.*;

import java.net.URI;
import java.net.URLEncoder;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DbNamingStrategyTest {
  private DbNamingStrategy strategy;

  @Before
  public void setUp() throws Exception {
    strategy = new DbNamingStrategy();
  }

  @Test
  public void testIt() throws Exception {
    String userId = "johannes@familieschneider.info";
    Bucket bucket = new Bucket( "public" );

    assertEquals( "johannes(at)familieschneider_info$public$data", strategy.getDatabaseName( userId, bucket, DbNamingStrategy.Type.DATA ) );
    assertEquals( "johannes(at)familieschneider_info$public$attachments", strategy.getDatabaseName( userId, bucket, DbNamingStrategy.Type.ATTACHMENTS ) );
    assertEquals( "johannes(at)familieschneider_info$other$attachments", strategy.getDatabaseName( userId, new Bucket( "other" ), DbNamingStrategy.Type.ATTACHMENTS ) );
  }

  @Test
  public void testEscaping() throws Exception {
    String userId = "johannes@familieschneider.info";
    Bucket bucket = new Bucket( "public" );

    URI server = new URI( "http://localhost:8080" );
    String dbName = strategy.getDatabaseName( userId, bucket, DbNamingStrategy.Type.ATTACHMENTS );
    assertEquals( "johannes(at)familieschneider_info$public$attachments", dbName );

    URI uri = server.resolve( "/" + dbName );

    assertEquals( "http://localhost:8080/johannes(at)familieschneider_info$public$attachments", uri.toString() );
    assertEquals( "/johannes(at)familieschneider_info$public$attachments", uri.getPath() );
    assertEquals( "/johannes(at)familieschneider_info$public$attachments", uri.getRawPath() );
  }

  @Test
  public void testUriBuilder() throws Exception {
    assertEquals( "http://localhost:8080/daDbName%2Fdada", new UriBuilderImpl().scheme( "http" ).host( "localhost" ).port( 8080 ).path( URLEncoder.encode( "daDbName/dada", "UTF-8" ) ).build().toString() );
  }

  @Test
  public void testUrlEncode() throws Exception {
    assertEquals( "dbname%2Fdata", URLEncoder.encode( "dbname/data", "UTF-8" ) );
  }
}
