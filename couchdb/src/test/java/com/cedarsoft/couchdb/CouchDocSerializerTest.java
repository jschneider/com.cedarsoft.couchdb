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

import com.cedarsoft.JsonUtils;
import com.cedarsoft.couchdb.test.Foo;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

/**
 *
 */
public class CouchDocSerializerTest {
  @Test
  public void testWithInner() throws Exception {
    Foo foo = new Foo( 7, "asdf" );
    Foo.Serializer fooSerializer = new Foo.Serializer();

    CouchDoc<Foo> info = new CouchDoc<Foo>( "daId", new Revision( "daRev" ), foo );

    CouchDocSerializer serializer = new CouchDocSerializer();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize( info, fooSerializer, out );

    JsonUtils.assertJsonEquals( "{\n" +
      "  \"_id\" : \"daId\",\n" +
      "  \"_rev\" : \"daRev\",\n" +
      "  \"@type\" : \"foo\",\n" +
      "  \"@version\" : \"1.0.0\"," +
      "  \"aValue\" : 7,\n" +
      "  \"description\" : \"asdf\"\n" +
      "}", out.toString() );

    CouchDoc<Foo> deserialized = serializer.deserialize( fooSerializer, new ByteArrayInputStream( out.toByteArray() ) );
    assertEquals( "daId", deserialized.getId() );
    assertEquals( "daRev", deserialized.getRev() );
    Assert.assertEquals( 7, deserialized.getObject().getaValue() );
    Assert.assertEquals( "asdf", deserialized.getObject().getDescription() );
  }

  @Test
  public void testNoRev() throws Exception {
    Foo foo = new Foo( 7, "asdf" );
    Foo.Serializer fooSerializer = new Foo.Serializer();

    CouchDoc<Foo> info = new CouchDoc<Foo>( "daId", foo );

    CouchDocSerializer serializer = new CouchDocSerializer();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize( info, fooSerializer, out );

    JsonUtils.assertJsonEquals( "{\n" +
      "  \"_id\" : \"daId\",\n" +
      "  \"@type\" : \"foo\",\n" +
      "  \"@version\" : \"1.0.0\"," +
      "  \"aValue\" : 7,\n" +
      "  \"description\" : \"asdf\"\n" +
      "}", out.toString() );

    //Deserialization is not necessary, since the rev is always set when fetching from CouchDB
  }
}
