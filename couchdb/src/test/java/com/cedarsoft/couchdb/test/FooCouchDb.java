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
import com.cedarsoft.couchdb.CouchDoc;
import com.cedarsoft.couchdb.io.CouchDocSerializer;
import com.cedarsoft.couchdb.CreationFailedException;
import com.cedarsoft.couchdb.io.CreationFailedExceptionSerializer;
import com.cedarsoft.couchdb.CreationResponse;
import com.cedarsoft.couchdb.io.CreationResponseSerializer;
import com.cedarsoft.couchdb.DocId;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.jcouchdb.db.Database;
import org.jcouchdb.db.Response;
import org.jcouchdb.db.Server;
import org.jcouchdb.db.ServerImpl;
import org.jcouchdb.exception.CouchDBException;
import org.jetbrains.annotations.NonNls;
import org.junit.*;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 *
 */
public class FooCouchDb {
  @NonNls
  public static final String HOST = "localhost";
  @NonNls
  public static final int PORT = 5984;
  @NonNls
  public static final String DB_NAME = "collustra_test3";

  private Server server;
  private Database database;

  @Before
  public void setUp() throws Exception {
    server = new ServerImpl( HOST, PORT );

    try {
      server.deleteDatabase( DB_NAME );
    } catch ( CouchDBException ignore ) {
    }

    assertTrue( server.createDatabase( DB_NAME ) );
    database = new Database( server, DB_NAME );
  }

  @Test
  public void testMVCC() throws Exception {
    Foo foo = new Foo( 42, "asdf" );
    Foo.Serializer fooSerializer = new Foo.Serializer();

    CouchDoc<Foo> info = new CouchDoc<Foo>( new DocId( "daId" ), foo );
    CouchDocSerializer serializer = new CouchDocSerializer();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize( info, fooSerializer, out );
    JsonUtils.assertJsonEquals( "{\n" +
      "  \"_id\" : \"daId\",\n" +
      "  \"@type\" : \"foo\",\n" +
      "  \"@version\" : \"1.0.0\"," +
      "  \"aValue\" : 42,\n" +
      "  \"description\" : \"asdf\"\n" +
      "}", out.toString() );

    String uri = "/" + database.getName() + "/" + info.getId();
    {
      Response response0 = server.put( uri, out.toByteArray(), MediaType.APPLICATION_JSON );
      JsonUtils.assertJsonEquals( "{\"ok\":true,\"id\":\"daId\",\"rev\":\"1-a61702329aa7cc6b870f7cfcc24aacac\"}", response0.getContentAsString() );
      assertEquals( 201, response0.getCode() );
    }

    {
      Response response1 = server.get( uri );
      String responseContent1 = response1.getContentAsString();
      JsonUtils.assertJsonEquals( "{\n" +
        "  \"_id\" : \"daId\",\n" +
        "  \"_rev\" : \"1-a61702329aa7cc6b870f7cfcc24aacac\",\n" +
        "  \"@type\" : \"foo\"," +
        "  \"@version\" : \"1.0.0\",\n" +
        "  \"aValue\" : 42,\n" +
        "  \"description\" : \"asdf\"\n" +
        "}", responseContent1 );
      assertEquals( 200, response1.getCode() );

      CouchDoc<Foo> deserialized = serializer.deserialize( fooSerializer, new ByteArrayInputStream( responseContent1.getBytes() ) );
      assertEquals( "daId", deserialized.getId() );
      assertEquals( "1-a61702329aa7cc6b870f7cfcc24aacac", deserialized.getRev() );
      assertEquals( 42, deserialized.getObject().getaValue() );
      assertEquals( "asdf", deserialized.getObject().getDescription() );

      Response response2 = server.put( uri, responseContent1.getBytes(), MediaType.APPLICATION_JSON );
      CreationResponse creationResponse = new CreationResponseSerializer().deserialize( response2.getInputStream() );
      assertEquals( "2-4ffec4730eec590d07f82789cbe991c6", creationResponse.getRev() );
      assertEquals( 201, response2.getCode() );
      assertEquals( deserialized.getId(), creationResponse.getId() );

      Response response3 = server.put( uri, responseContent1.getBytes(), MediaType.APPLICATION_JSON );
      CreationFailedException creationFailedResponse1 = new CreationFailedExceptionSerializer().deserialize( response3.getInputStream() );
      assertEquals( "conflict", creationFailedResponse1.getError() );
      assertEquals( "Document update conflict.", creationFailedResponse1.getReason() );
      assertEquals( 409, response3.getCode() );
    }
  }

  @Test
  public void run() throws IOException {
    Foo foo = new Foo( 42, "asdf" );
    Foo.Serializer serializer = new Foo.Serializer();

    byte[] serialized = serializer.serializeToByteArray( foo );
    JsonUtils.assertJsonEquals( "{\n" +
      "  \"@type\" : \"foo\",\n" +
      "\"@version\" : \"1.0.0\"," +
      "  \"aValue\" : 42,\n" +
      "  \"description\" : \"asdf\"\n" +
      "}", new String( serialized ) );

    Client client = new Client();
    WebResource server = client.resource( "http://" + HOST + ":" + PORT );
    WebResource db = server.path( DB_NAME );

    {
      ClientResponse response = db.path( "daDoc" ).put( ClientResponse.class, serialized );
      String responseAsString = response.getEntity( String.class );
      if ( response.getStatus() != 201 ) {
        fail( responseAsString + " " + response.getStatus() );
      }
      assertEquals( 201, response.getStatus() );
      JsonUtils.assertJsonEquals( "{\"ok\":true,\"id\":\"daDoc\",\"rev\":\"1-a61702329aa7cc6b870f7cfcc24aacac\"}", responseAsString );

      CreationResponse creationResponse = new CreationResponseSerializer().deserialize( new ByteArrayInputStream( responseAsString.getBytes() ) );
      assertNotNull( creationResponse );
      assertEquals( "daDoc", creationResponse.getId() );
      assertEquals( "1-a61702329aa7cc6b870f7cfcc24aacac", creationResponse.getRev() );
    }


    //Conflict!
    ClientResponse response = db.path( "daDoc" ).put( ClientResponse.class, serialized );
    String responseAsString = response.getEntity( String.class );
    if ( response.getStatus() != 409 ) {
      fail( responseAsString + " " + response.getStatus() );
    }

    assertEquals( 409, response.getStatus() );
    JsonUtils.assertJsonEquals( "{\n" +
      "  \"error\" : \"conflict\",\n" +
      "  \"reason\" : \"Document update conflict.\"\n" +
      "}", responseAsString );
  }

  @Test
  public void runIt() throws IOException {
    Foo foo = new Foo( 42, "asdf" );
    Foo.Serializer serializer = new Foo.Serializer();

    byte[] serialized = serializer.serializeToByteArray( foo );
    JsonUtils.assertJsonEquals( "{\n" +
      "  \"@type\" : \"foo\"," +
      "\"@version\" : \"1.0.0\",\n" +
      "  \"aValue\" : 42,\n" +
      "  \"description\" : \"asdf\"\n" +
      "}", new String( serialized ) );

    Client client = new Client();
    WebResource server = client.resource( "http://" + HOST + ":" + PORT );
    WebResource db = server.path( DB_NAME );

    {
      ClientResponse response = db.path( "daDoc" ).put( ClientResponse.class, serialized );
      CreationResponse creationResponse = new CreationResponseSerializer().deserialize( response.getEntityInputStream() );

      assertEquals( "daDoc", creationResponse.getId() );
      assertEquals( "1-a61702329aa7cc6b870f7cfcc24aacac", creationResponse.getRev() );
    }
  }
}