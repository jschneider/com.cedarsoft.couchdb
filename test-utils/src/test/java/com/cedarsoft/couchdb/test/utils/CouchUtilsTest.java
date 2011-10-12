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

import com.cedarsoft.couchdb.ActionFailedException;
import com.cedarsoft.couchdb.CouchDatabase;
import com.cedarsoft.couchdb.CouchUtils;
import com.cedarsoft.couchdb.DocId;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.fest.reflect.core.Reflection;
import org.jcouchdb.db.Database;
import org.jcouchdb.document.DesignDocument;
import org.junit.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchUtilsTest extends CouchTest {

  public static final String DB_NAME = "couchutilstest";

  @Test
  public void testBasic() throws Exception {
    CouchDatabase database = couchDbRule.getCouchDatabaseObject( DB_NAME );

    CouchUtils couchUtils = new CouchUtils( database );
    assertThat( couchUtils.exists() ).isFalse();

    try {
      database.get( new DocId( "dasdf" ) );
      fail( "Where is the Exception" );
    } catch ( ActionFailedException e ) {
      assertThat( e.getStatus() ).isEqualTo( 404 );
      assertThat( e.getReason() ).isEqualTo( "no_db_file" );
      assertThat( e.getMessage() ).isEqualTo( "404 not_found: no_db_file" );
    }

    assertThat( couchUtils.exists() ).isFalse();
    couchUtils.create();
    assertThat( couchUtils.exists() ).isTrue();
    couchDbRule.deleteDb( database.getDbName() );
  }

  @Test
  public void testUrl() throws Exception {
    CouchDatabase database = couchDbRule.getCouchDatabaseObject( DB_NAME );
    Database internalDb = new CouchUtils( database ).createInternalDb();

    assertThat( internalDb.getName() ).isEqualTo( DB_NAME );

    String serverURI = Reflection.field( "serverURI" ).ofType( String.class ).in( internalDb.getServer() ).get();
    assertThat( serverURI ).startsWith( "http://" );
  }

  @Test
  public void testViews() throws Exception {
    CouchDatabase db = createDb( DB_NAME );

    CouchUtils utils = new CouchUtils( db );
    utils.uploadViews( getClass().getResource( "sampleView.map.js" ) );

    DesignDocument designDocument = utils.createInternalDb().getDesignDocument( "utils" );
    assertThat( designDocument.getViews() ).hasSize( 1 );
  }

  @Test
  public void testExtractCredentials() throws Exception {
    HTTPBasicAuthFilter filter = new HTTPBasicAuthFilter( "daUser", "daPass" );
    UsernamePasswordCredentials credentials = CouchUtils.extractCredentials( filter );

    assertThat( credentials.getUserName() ).isEqualTo( "daUser" );
    assertThat( credentials.getPassword() ).isEqualTo( "daPass" );
  }
}
