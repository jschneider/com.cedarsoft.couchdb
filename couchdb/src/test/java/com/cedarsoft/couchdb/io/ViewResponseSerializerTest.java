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

package com.cedarsoft.couchdb.io;

import com.cedarsoft.couchdb.core.DocId;
import com.cedarsoft.couchdb.core.Row;
import com.cedarsoft.couchdb.core.ViewResponse;
import com.cedarsoft.couchdb.test.Foo;
import com.cedarsoft.serialization.jackson.ListSerializer;
import com.cedarsoft.serialization.jackson.NullSerializer;
import com.cedarsoft.serialization.jackson.StringSerializer;
import com.cedarsoft.test.utils.JsonUtils;
import com.google.common.io.ByteStreams;
import org.fest.assertions.Assertions;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ViewResponseSerializerTest {
  private Foo.Serializer fooSerializer;
  private ViewResponseSerializer serializer;

  @Before
  public void setUp() throws Exception {
    fooSerializer = new Foo.Serializer();
    serializer = new ViewResponseSerializer( new RowSerializer() );
  }

  @Test
  public void testTypeId() throws Exception {
    URL resource = getClass().getResource( "ViewResponse.type.id.json" );

    ViewResponse<List<? extends Object>, Void, ?> response = serializer.deserialize( new ListSerializer(), NullSerializer.INSTANCE, resource.openStream() );
    assertEquals( 2, response.getTotalRows() );
    assertEquals( 0, response.getOffset() );
    assertEquals( 2, response.getRows().size() );

    assertEquals( "lens_canon_ef_35mm_f-1.4l", response.getRows().get( 0 ).getId().asString() );
    assertEquals( null, response.getRows().get( 0 ).getValue() );
    assertEquals( 2, response.getRows().get( 0 ).getKey().size() );
    assertEquals( "lens", response.getRows().get( 0 ).getKey().get( 0 ) );
    assertEquals( "canon_ef_35mm_f-1.4l", response.getRows().get( 0 ).getKey().get( 1 ) );

    assertEquals( "lens_canon_ef_70-200mm_f-2.8l_is", response.getRows().get( 1 ).getId().asString() );
    assertEquals( null, response.getRows().get( 1 ).getValue() );
    assertEquals( 2, response.getRows().get( 1 ).getKey().size() );
    assertEquals( "lens", response.getRows().get( 1 ).getKey().get( 0 ) );
    assertEquals( "canon_ef_70-200mm_f-2.8l_is", response.getRows().get( 1 ).getKey().get( 1 ) );
  }

  @Test
  public void testWithDocId() throws Exception {
    URL resource = getClass().getResource( "ViewResponse.type.id.include_docs.json" );

    ViewResponse<List<? extends Object>, Void, ?> response = serializer.deserialize( new ListSerializer(), NullSerializer.INSTANCE, new Foo.Serializer(), resource.openStream() );
    assertEquals( 2, response.getTotalRows() );
    assertEquals( 0, response.getOffset() );
    assertEquals( 2, response.getRows().size() );

    assertNotNull( response.getRows().get( 0 ).getDoc() );
    assertEquals( "lens_canon_ef_35mm_f-1.4l", response.getRows().get( 0 ).getDoc().getId().asString() );
    Assert.assertEquals( 7, ( ( Foo ) response.getRows().get( 0 ).getDoc().getObject() ).getaValue() );
    Assert.assertEquals( "daDescription", ( ( Foo ) response.getRows().get( 0 ).getDoc().getObject() ).getDescription() );
  }

  @Test
  public void testFooList() throws Exception {
    ViewResponse<Foo, List<? extends String>, ?> viewResponse = new ViewResponse<Foo, List<? extends String>, Void>( 777, 12, Arrays.asList(
      new Row<Foo, List<? extends String>, Void>( new DocId( "daId0" ), new Foo( 8, "daKeyFoo" ), Arrays.asList( "a", "b" ) ),
      new Row<Foo, List<? extends String>, Void>( new DocId( "daId1" ), new Foo( 10, "daKeyFoo" ), Arrays.asList( "c", "d" ) )
    ) );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize( viewResponse, this.fooSerializer, new ListSerializer(), out );

    JsonUtils.assertJsonEquals(getClass().getResource("ViewResponse.foo.stringList.json"), out.toString());

    ViewResponse<Foo, List<? extends String>, ?> deserialized = serializer.deserialize( fooSerializer, new ListSerializer(), new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( viewResponse.getTotalRows(), deserialized.getTotalRows() );
    assertEquals( viewResponse.getOffset(), deserialized.getOffset() );
    assertEquals( viewResponse.getRows(), deserialized.getRows() );
  }

  @Test
  public void testFooFoo() throws Exception {
    ViewResponse<Foo, Foo, Void> viewResponse = new ViewResponse<Foo, Foo, Void>( 777, 12, Arrays.asList(
      new Row<Foo, Foo, Void>( new DocId( "daId0" ), new Foo( 8, "daKeyFoo" ), new Foo( 9, "daValueFoo" ) ),
      new Row<Foo, Foo, Void>( new DocId( "daId1" ), new Foo( 10, "daKeyFoo" ), new Foo( 11, "daValueFoo" ) )
    ) );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize( viewResponse, this.fooSerializer, this.fooSerializer, out );

    JsonUtils.assertJsonEquals( getClass().getResource( "ViewResponse.foo.foo.json" ), out.toString() );

    ViewResponse<Foo, Foo, ?> deserialized = serializer.deserialize( fooSerializer, fooSerializer, new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( viewResponse.getTotalRows(), deserialized.getTotalRows() );
    assertEquals( viewResponse.getOffset(), deserialized.getOffset() );
    assertEquals( viewResponse.getRows(), deserialized.getRows() );
  }

  @Test
  public void testFooString() throws Exception {
    ViewResponse<Foo, String, Void> viewResponse = new ViewResponse<Foo, String, Void>( 777, 12, Arrays.asList(
      new Row<Foo, String, Void>( new DocId( "daId0" ), new Foo( 8, "daKeyFoo" ), "daValue0" ),
      new Row<Foo, String, Void>( new DocId( "daId1" ), new Foo( 10, "daKeyFoo" ), "daValue1" )
    ) );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize( viewResponse, this.fooSerializer, new StringSerializer(), out );

    JsonUtils.assertJsonEquals( getClass().getResource( "ViewResponse.foo.string.json" ), out.toString() );

    ViewResponse<Foo, String, ?> deserialized = serializer.deserialize( fooSerializer, new StringSerializer(), new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( viewResponse.getTotalRows(), deserialized.getTotalRows() );
    assertEquals( viewResponse.getOffset(), deserialized.getOffset() );
    assertEquals( viewResponse.getRows(), deserialized.getRows() );
  }

  @Test
  public void testStringString() throws Exception {
    ViewResponse<String, String, Void> viewResponse = new ViewResponse<String, String, Void>( 777, 12, Arrays.asList(
      new Row<String, String, Void>( new DocId( "daId0" ), "daKey", "daValue" ),
      new Row<String, String, Void>( new DocId( "daId1" ), "daOtherKey", "daOtherValue" )
    ) );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StringSerializer stringSerializer = new StringSerializer();
    serializer.serialize( viewResponse, stringSerializer, stringSerializer, out );

    JsonUtils.assertJsonEquals( getClass().getResource( "ViewResponse.string.string.json" ), out.toString() );

    ViewResponse<String, String, ?> deserialized = serializer.deserialize( stringSerializer, stringSerializer, new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( viewResponse.getTotalRows(), deserialized.getTotalRows() );
    assertEquals( viewResponse.getOffset(), deserialized.getOffset() );
    assertEquals( viewResponse.getRows(), deserialized.getRows() );
  }

  @Test
  public void testReduced() throws Exception {
    ViewResponse<List<? extends Object>, Void, ?> response = serializer.deserialize( new ListSerializer(), NullSerializer.INSTANCE, getClass().getResourceAsStream( "ViewResponse.reduced.foo.json" ) );
    assertEquals( -1, response.getTotalRows() );
    assertEquals( -1, response.getOffset() );
    assertEquals( 2, response.getRows().size() );

    assertEquals( "lens_canon_ef_35mm_f-1.4l", response.getRows().get( 0 ).getId().asString() );
  }
}
