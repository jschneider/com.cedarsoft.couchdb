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

import com.cedarsoft.couchdb.core.ActionFailedException;
import com.cedarsoft.couchdb.ActionResponse;
import com.cedarsoft.couchdb.core.CouchDoc;
import com.cedarsoft.couchdb.core.DocId;
import com.cedarsoft.couchdb.core.UniqueId;
import com.cedarsoft.couchdb.io.ActionFailedExceptionSerializer;
import com.cedarsoft.couchdb.io.CouchDocSerializer;
import com.cedarsoft.couchdb.io.ActionResponseSerializer;
import com.cedarsoft.test.utils.JsonUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.*;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 *
 */
public class FooCouchDbTest extends CouchTest {
  @Test
  public void testMVCC() throws Exception {
    Foo foo = new Foo(42, "asdf");
    Foo.Serializer fooSerializer = new Foo.Serializer();

    CouchDoc<Foo> info = new CouchDoc<Foo>(new DocId("daId"), foo);
    CouchDocSerializer serializer = new CouchDocSerializer();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize(info, fooSerializer, out);
    JsonUtils.assertJsonEquals("{\n" +
                                 "  \"_id\" : \"daId\",\n" +
                                 "  \"@type\" : \"foo\",\n" +
                                 "  \"@version\" : \"1.0.0\"," +
                                 "  \"aValue\" : 42,\n" +
                                 "  \"description\" : \"asdf\"\n" +
                                 "}", out.toString());

    String uri = "/" + db().getDbName() + "/" + info.getId();
    {
      ClientResponse response0 = server().put( uri, out.toByteArray( ), MediaType.APPLICATION_JSON );
      try {
        JsonUtils.assertJsonEquals( "{\"ok\":true,\"id\":\"daId\",\"rev\":\"1-a61702329aa7cc6b870f7cfcc24aacac\"}", response0.getEntity( String.class ) );
        assertEquals(201, response0.getStatus( ));
      } finally {
        response0.close();
      }
    }

    {
      ClientResponse response1 = server().get( uri );
      String responseContent1 = response1.getEntity( String.class );
      JsonUtils.assertJsonEquals("{\n" +
                                   "  \"_id\" : \"daId\",\n" +
                                   "  \"_rev\" : \"1-a61702329aa7cc6b870f7cfcc24aacac\",\n" +
                                   "  \"@type\" : \"foo\"," +
                                   "  \"@version\" : \"1.0.0\",\n" +
                                   "  \"aValue\" : 42,\n" +
                                   "  \"description\" : \"asdf\"\n" +
                                   "}", responseContent1);
      assertEquals(200, response1.getStatus( ));

      CouchDoc<Foo> deserialized = serializer.deserialize(fooSerializer, new ByteArrayInputStream(responseContent1.getBytes()));
      assertEquals("daId", deserialized.getId().asString());
      assertEquals("1-a61702329aa7cc6b870f7cfcc24aacac", deserialized.getRev().asString());
      assertEquals(42, deserialized.getObject().getaValue());
      assertEquals("asdf", deserialized.getObject().getDescription());

      ClientResponse response2 = server().put( uri, responseContent1.getBytes( ), MediaType.APPLICATION_JSON );
      try {
        UniqueId actionResponse = new ActionResponseSerializer().deserialize( response2.getEntity( InputStream.class ) );
        assertEquals("2-4ffec4730eec590d07f82789cbe991c6", actionResponse.getRevision().asString());
        assertEquals(201, response2.getStatus( ));
        assertEquals(deserialized.getId(), actionResponse.getId());
      } finally {
        response2.close();
      }

      ClientResponse response3 = server().put( uri, responseContent1.getBytes( ), MediaType.APPLICATION_JSON );
      try {
        ActionFailedException actionFailedResponse1 = new ActionFailedExceptionSerializer().deserialize(response3.getStatus( ), response3.getEntity( InputStream.class  ));
        assertEquals("conflict", actionFailedResponse1.getError());
        assertEquals("Document update conflict.", actionFailedResponse1.getReason());
        assertEquals(409, response3.getStatus( ));
      } finally {
        response3.close();
      }
    }
  }

  @Test
  public void run() throws IOException {
    Foo foo = new Foo(42, "asdf");
    Foo.Serializer serializer = new Foo.Serializer();

    byte[] serialized = serializer.serializeToByteArray(foo);
    JsonUtils.assertJsonEquals("{\n" +
                                 "  \"@type\" : \"foo\",\n" +
                                 "\"@version\" : \"1.0.0\"," +
                                 "  \"aValue\" : 42,\n" +
                                 "  \"description\" : \"asdf\"\n" +
                                 "}", new String(serialized));

    Client client = new Client();

    WebResource server = client.resource(db().getURI());
    WebResource db = server;
    {
      ClientResponse response = db.path("daDoc").put(ClientResponse.class, serialized);
      String responseAsString = response.getEntity(String.class);
      if (response.getStatus() != 201) {
        fail(responseAsString + " " + response.getStatus());
      }
      assertEquals(201, response.getStatus());
      JsonUtils.assertJsonEquals("{\"ok\":true,\"id\":\"daDoc\",\"rev\":\"1-a61702329aa7cc6b870f7cfcc24aacac\"}", responseAsString);

      UniqueId actionResponse = new ActionResponseSerializer().deserialize( new ByteArrayInputStream( responseAsString.getBytes() ) );
      assertNotNull(actionResponse);
      assertEquals("daDoc", actionResponse.getId().asString());
      assertEquals("1-a61702329aa7cc6b870f7cfcc24aacac", actionResponse.getRev().asString());
    }


    //Conflict!
    ClientResponse response = db.path("daDoc").put(ClientResponse.class, serialized);
    String responseAsString = response.getEntity(String.class);
    if (response.getStatus() != 409) {
      fail(responseAsString + " " + response.getStatus());
    }

    assertEquals(409, response.getStatus());
    JsonUtils.assertJsonEquals("{\n" +
                                 "  \"error\" : \"conflict\",\n" +
                                 "  \"reason\" : \"Document update conflict.\"\n" +
                                 "}", responseAsString);
  }

  @Test
  public void runIt() throws IOException {
    Foo foo = new Foo(42, "asdf");
    Foo.Serializer serializer = new Foo.Serializer();

    byte[] serialized = serializer.serializeToByteArray(foo);
    JsonUtils.assertJsonEquals("{\n" +
                                 "  \"@type\" : \"foo\"," +
                                 "\"@version\" : \"1.0.0\",\n" +
                                 "  \"aValue\" : 42,\n" +
                                 "  \"description\" : \"asdf\"\n" +
                                 "}", new String(serialized));

    Client client = new Client();
    WebResource server = client.resource(db().getURI());
    WebResource db = server;

    {
      ClientResponse response = db.path("daDoc").accept( MediaType.APPLICATION_JSON_TYPE ).put(ClientResponse.class, serialized);
      ActionResponse actionResponse = new ActionResponseSerializer().deserialize(response);

      assertEquals("daDoc", actionResponse.getId().asString());
      assertEquals("1-a61702329aa7cc6b870f7cfcc24aacac", actionResponse.getRev().asString());
    }
  }
}
