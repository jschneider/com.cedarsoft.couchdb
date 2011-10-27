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

import com.cedarsoft.couchdb.DesignDocument;
import com.cedarsoft.couchdb.DesignDocuments;
import com.cedarsoft.couchdb.DesignDocumentsUpdater;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.*;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DesignDocumentsUpdaterTest extends CouchTest {
  @Test
  public void testIt( ) throws Exception {
    assertThat( db( ) ).isNotNull( );

    List<? extends DesignDocument> designDocuments = DesignDocuments.createDesignDocuments( getClass( ).getResource( "views/doc1/file1.map.js" ) );
    assertThat( designDocuments ).hasSize( 3 );

    DesignDocumentsUpdater updater = new DesignDocumentsUpdater( db( ) );
    updater.update( designDocuments );

    {
      ClientResponse response = db( ).getDbRoot( ).path( "_design/doc1" ).get( ClientResponse.class );
      assertThat( response ).isNotNull( );
      assertThat( response.getStatus( ) ).isEqualTo( 200 );
    }
    {
      ClientResponse response = db( ).getDbRoot( ).path( "_design/doc1/_view/file1" ).get( ClientResponse.class );
      assertThat( response ).isNotNull( );
      assertThat( response.getStatus( ) ).isEqualTo( 200 );
    }
  }
}