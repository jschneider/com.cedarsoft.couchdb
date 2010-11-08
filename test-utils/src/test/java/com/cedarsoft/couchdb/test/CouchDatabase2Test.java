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

package com.cedarsoft.couchdb.test;

import com.cedarsoft.JsonUtils;
import com.cedarsoft.couchdb.Bar;
import com.cedarsoft.couchdb.CouchDbTest;
import com.cedarsoft.couchdb.CouchDoc;
import com.cedarsoft.couchdb.CreationFailedException;
import com.cedarsoft.couchdb.CreationResponse;
import com.cedarsoft.couchdb.DeletionFailedException;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.couchdb.io.CouchDocSerializer;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.jetbrains.annotations.NonNls;
import org.junit.*;
import org.junit.rules.*;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchDatabase2Test extends CouchDbTest {
  @NonNls
  private static final Revision REV = new Revision( "1-8908180fecd9b17657889f91973f89eb" );

  private CouchDocSerializer couchDocSerializer;
  private final Bar.Serializer serializer = new Bar.Serializer();

  @Before
  public void setup() throws URISyntaxException {
    couchDocSerializer = new CouchDocSerializer();
  }

  @Test
  public void testManually() throws Exception {
    CreationResponse response = db.put( new CouchDoc<Bar>( new DocId( "daId" ), new Bar( 7, "hey" ) ), serializer );
    assertEquals( "daId", response.getId().asString() );
    assertEquals( REV, response.getRev() );

    {
      byte[] read = ByteStreams.toByteArray( db.get( new DocId( "daId" ) ) );
      JsonUtils.assertJsonEquals( getClass().getResource( "bar.json" ), new String( read ) );

      CouchDoc<Bar> doc = couchDocSerializer.deserialize( serializer, new ByteArrayInputStream( read ) );
      assertEquals( "daId", doc.getId().asString() );
      assertEquals( REV, doc.getRev() );
      assertEquals( 7, doc.getObject().getValue() );
      assertEquals( "hey", doc.getObject().getDescription() );
    }
  }

  @Test
  public void testDocRevUpdated() throws Exception {
    CouchDoc<Bar> doc = new CouchDoc<Bar>( new DocId( "daId" ), new Bar( 7, "hey" ) );
    assertNull( doc.getRev() );
    CreationResponse response = db.put( doc, serializer );
    assertNotNull( doc.getRev() );
    assertEquals( REV, doc.getRev() );
  }

  @Test
  public void testCreateGetDelete() throws Exception {
    {
      CreationResponse response = db.put( new CouchDoc<Bar>( new DocId( "daId" ), new Bar( 7, "hey" ) ), serializer );

      assertEquals( "daId", response.getId().asString() );
      assertEquals( REV, response.getRev() );
    }

    DocId id = new DocId( "daId" );
    {
      CouchDoc<Bar> doc = db.get( id, serializer );
      assertEquals( "daId", doc.getId().asString() );
      assertEquals( REV, doc.getRev() );
      assertEquals( 7, doc.getObject().getValue() );
    }

    db.delete( id, REV );


    //Check deleted
    try {
      db.get( id, serializer );
    } catch ( UniformInterfaceException e ) {
      assertEquals( 404, e.getResponse().getStatus() );
      assertEquals( "{\"error\":\"not_found\",\"reason\":\"deleted\"}", e.getResponse().getEntity( String.class ).trim() );
    }


    try {
      db.delete( id, REV );
      fail( "Where is the Exception" );
    } catch ( DeletionFailedException e ) {
      assertEquals( "not_found", e.getError() );
      assertEquals( "deleted", e.getReason() );
    }
  }

  @Test
  public void testUpdated() throws Exception {
    //Create
    DocId id = new DocId( "daId" );
    {
      db.put( new CouchDoc<Bar>( id, new Bar( 7, "hey" ) ), serializer );
    }

    //fetch
    {
      CouchDoc<Bar> doc = db.get( id, serializer );
      Bar bar = doc.getObject();

      assertEquals( "hey", bar.getDescription() );
      assertEquals( 7, bar.getValue() );

      bar.setDescription( "updatedDescription" );
      bar.setValue( 42 );

      //Update
      db.putUpdated( doc, serializer );
    }

    //fetch2
    {
      CouchDoc<Bar> doc = db.get( id, serializer );
      Bar bar = doc.getObject();

      assertEquals( "updatedDescription", bar.getDescription() );
      assertEquals( 42, bar.getValue() );

      bar.setDescription( "updatedDescription2" );
      bar.setValue( 11 );

      //Update
      db.putUpdated( doc, serializer );
    }

    {
      CouchDoc<Bar> doc = db.get( id, serializer );
      Bar bar = doc.getObject();

      assertEquals( "updatedDescription2", bar.getDescription() );
      assertEquals( 11, bar.getValue() );
    }

    //Invalid updated without ref
    expectedException.expect( CreationFailedException.class );
    expectedException.expectMessage( "conflict: Document update conflict." );

    db.put( new CouchDoc<Bar>( id, new Bar( 1, "should not work!" ) ), serializer );
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
}
