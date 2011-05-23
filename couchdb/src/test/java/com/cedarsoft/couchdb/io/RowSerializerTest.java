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

import com.cedarsoft.JsonUtils;
import com.cedarsoft.couchdb.CouchDoc;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.couchdb.Row;
import com.cedarsoft.couchdb.test.Foo;
import com.cedarsoft.serialization.jackson.ListSerializer;
import com.cedarsoft.serialization.jackson.NullSerializer;
import com.cedarsoft.serialization.jackson.StringSerializer;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class RowSerializerTest {
  private URL resource;

  @Before
  public void setUp() throws Exception {
    resource = getClass().getResource( "row.json" );
  }

  @Test
  public void testNullValue() throws Exception {
    Row<String, Void, ?> row = new Row<String, Void, Void>( new DocId( "daId" ), "daKey", null );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new RowSerializer().serialize( row, new StringSerializer(), NullSerializer.INSTANCE, out );

    JsonUtils.assertJsonEquals( "{\n" +
                                  "  \"id\":\"daId\",\n" +
                                  "  \"key\":\"daKey\",\n" +
                                  "  \"value\":null\n" +
                                  "}", out.toString() );

    Row<String, Void, ?> deserialized = new RowSerializer().deserialize( new StringSerializer(), NullSerializer.INSTANCE, new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( "daId", deserialized.getId().asString() );
    assertNotNull( deserialized.getKey() );
    Void value = deserialized.getValue();
    assertNull( value );
  }

  @Test
  public void testSerializeRow() throws Exception {
    Row<String, String, ?> row = new Row<String, String, Void>( new DocId( "daId" ), "daKey", "daValue" );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new RowSerializer().serialize( row, new StringSerializer(), new StringSerializer(), out );

    JsonUtils.assertJsonEquals( "{\n" +
                                  "  \"id\":\"daId\",\n" +
                                  "  \"key\":\"daKey\",\n" +
                                  "  \"value\":\"daValue\"\n" +
                                  "}", out.toString() );


    Row<String, String, ?> deserialized = new RowSerializer().deserialize( new StringSerializer(), new StringSerializer(), new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( "daId", deserialized.getId().asString() );
    assertNotNull( deserialized.getKey() );
    assertNotNull( deserialized.getValue() );
    assertEquals( "daKey", deserialized.getKey() );
    assertEquals( "daValue", deserialized.getValue() );
  }

  @Test
  public void testFooValue() throws Exception {
    Row<String, Foo, ?> row = new Row<String, Foo, Void>( new DocId( "daId" ), "daKey", new Foo( 7, "asdf" ) );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new RowSerializer().serialize( row, new StringSerializer(), new Foo.Serializer(), out );

    JsonUtils.assertJsonEquals( getClass().getResource( "Row.foo.json" ), out.toString() );

    Row<String, Foo, ?> deserialized = new RowSerializer().deserialize( new StringSerializer(), new Foo.Serializer(), new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( "daId", deserialized.getId().asString() );
    assertNotNull( deserialized.getKey() );
    assertNotNull( deserialized.getValue() );
    assertEquals( "daKey", deserialized.getKey() );
    Assert.assertEquals( 7, deserialized.getValue().getaValue() );
    Assert.assertEquals( "asdf", deserialized.getValue().getDescription() );
  }

  @Test
  public void testFooKeyAndValue() throws Exception {
    Row<Foo, Foo, ?> row = new Row<Foo, Foo, Void>( new DocId( "daId" ), new Foo( 4, "inKey" ), new Foo( 7, "asdf" ) );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new RowSerializer().serialize( row, new Foo.Serializer(), new Foo.Serializer(), out );

    JsonUtils.assertJsonEquals( getClass().getResource( "Row.foo.foo.json" ), out.toString() );


    Row<Foo, Foo, ?> deserialized = new RowSerializer().deserialize( new Foo.Serializer(), new Foo.Serializer(), new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( "daId", deserialized.getId().asString() );
    assertNotNull( deserialized.getKey() );
    assertNotNull( deserialized.getValue() );
    Assert.assertEquals( 4, deserialized.getKey().getaValue() );
    Assert.assertEquals( 7, deserialized.getValue().getaValue() );
    Assert.assertEquals( "asdf", deserialized.getValue().getDescription() );
  }

  @Test
  public void testStringColl() throws Exception {
    Row<List<? extends String>, Foo, ?> row = new Row<List<? extends String>, Foo, Void>( new DocId( "daId" ), Arrays.asList( "key1", "key2", "key3" ), new Foo( 7, "asdf" ) );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new RowSerializer().serialize( row, new ListSerializer(), new Foo.Serializer(), out );

    JsonUtils.assertJsonEquals( getClass().getResource( "Row.stringColl.foo.json" ), out.toString() );


    Row<List<? extends String>, Foo, ?> deserialized = new RowSerializer().deserialize( new ListSerializer(), new Foo.Serializer(), new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( "daId", deserialized.getId().asString() );
    assertNotNull( deserialized.getKey() );
    assertNotNull( deserialized.getValue() );
    assertEquals( Arrays.asList( "key1", "key2", "key3" ), deserialized.getKey() );
    Assert.assertEquals( 7, deserialized.getValue().getaValue() );
    Assert.assertEquals( "asdf", deserialized.getValue().getDescription() );
  }

  @Test
  public void testInclDoc() throws Exception {
    CouchDoc<Foo> info = new CouchDoc<Foo>( new DocId( "daFooId" ), new Revision( "daRev" ), new Foo( 7, "daDescription" ) );
    Row<List<? extends String>, Foo, Foo> row = new Row<List<? extends String>, Foo, Foo>( new DocId( "daId" ), Arrays.asList( "key1", "key2", "key3" ), new Foo( 7, "asdf" ), info );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new RowSerializer().serialize( row, new ListSerializer(), new Foo.Serializer(), new Foo.Serializer(), out );
    JsonUtils.assertJsonEquals( getClass().getResource( "Row.stringColl.foo.include_docs.json" ), out.toString() );

    Row<List<? extends String>, Foo, Foo> deserialized = new RowSerializer().deserialize( new ListSerializer(), new Foo.Serializer(), new Foo.Serializer(), new ByteArrayInputStream( out.toByteArray() ) );
    assertNotNull( deserialized );

    assertEquals( "daId", deserialized.getId().asString() );
    assertNotNull( deserialized.getKey() );
    assertNotNull( deserialized.getValue() );
    assertEquals( Arrays.asList( "key1", "key2", "key3" ), deserialized.getKey() );
    Assert.assertEquals( 7, deserialized.getValue().getaValue() );
    Assert.assertEquals( "asdf", deserialized.getValue().getDescription() );

    assertNotNull( deserialized.getDoc() );
    assertEquals( "daFooId", deserialized.getDoc().getId().asString() );
    assertEquals( "daRev", deserialized.getDoc().getRev().asString() );
    Assert.assertEquals( 7, deserialized.getDoc().getObject().getaValue() );
    Assert.assertEquals( "daDescription", deserialized.getDoc().getObject().getDescription() );
  }
}
