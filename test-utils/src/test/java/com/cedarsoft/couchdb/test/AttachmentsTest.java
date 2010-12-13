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

import com.cedarsoft.AssertUtils;
import com.cedarsoft.JsonUtils;
import com.cedarsoft.Version;
import com.cedarsoft.VersionException;
import com.cedarsoft.VersionRange;
import com.cedarsoft.couchdb.ActionFailedException;
import com.cedarsoft.couchdb.ActionResponse;
import com.cedarsoft.couchdb.AttachmentId;
import com.cedarsoft.couchdb.CouchDoc;
import com.cedarsoft.couchdb.CouchTest;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.couchdb.io.RawCouchDocSerializer;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.*;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class AttachmentsTest extends CouchTest {
  @NonNls
  public static final String REV_1 = "1-4b8635c26c5b91bd2bc658ed866c727a";
  @NonNls
  public static final String REV_2 = "2-929f2959f8e81ed6b6c7784bee926065";
  @NotNull
  public static final String REV_3 = "3-46baf5940bdf721b5a1c590de66bfd94";
  @NotNull
  public static final String REV_4 = "4-0686197065ccc1e0a310e894da9c8dd7";
  @NotNull
  public static final DocId DOC_ID = new DocId( "daDocId" );

  private RawCouchDocSerializer serializer;

  @Before
  public void setup() throws URISyntaxException {
    serializer = new RawCouchDocSerializer();
  }

  @Test
  public void testInlined() throws Exception {
    CouchDoc<String> doc = new CouchDoc<String>( new DocId( "asdf" ), "the object" );
    doc.addAttachment( new CouchDoc.InlineAttachment( "daid", MediaType.TEXT_XML_TYPE, "<x/>".getBytes() ) );
    doc.addAttachment( new CouchDoc.InlineAttachment( "hehe", MediaType.APPLICATION_XML_TYPE, "<x2/>".getBytes() ) );

    AbstractJacksonSerializer<String> serializer = new AbstractJacksonSerializer<String>( "daString", VersionRange.single( 1, 0, 0 ) ) {
      @Override
      public void serialize( @NotNull JsonGenerator serializeTo, @NotNull String object, @NotNull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
        serializeTo.writeStringField( "daString", object );
      }

      @NotNull
      @Override
      public String deserialize( @NotNull JsonParser deserializeFrom, @NotNull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
        deserializeFrom.nextToken();
        deserializeFrom.nextValue();

        try {
          return deserializeFrom.getText();
        } finally {
          deserializeFrom.nextToken();
        }
      }
    };
    assertEquals( 201, db.put( doc, serializer ).getStatus() );

    JsonUtils.assertJsonEquals( getClass().getResource( "doc_with_2_attachments.json" ), new String( ByteStreams.toByteArray( db.get( doc.getId() ) ) ) );

    CouchDoc<String> resolvedDoc = db.get( doc.getId(), serializer );
    assertEquals( "the object", resolvedDoc.getObject() );
    assertEquals( 2, resolvedDoc.getAttachments().size() );
    assertEquals( "daid", resolvedDoc.getAttachments().get( 0 ).getId() );
    assertEquals( "text/xml", resolvedDoc.getAttachments().get( 0 ).getContentType().toString() );
    assertEquals( "hehe", resolvedDoc.getAttachments().get( 1 ).getId() );
    assertEquals( "application/xml", resolvedDoc.getAttachments().get( 1 ).getContentType().toString() );
  }

  @Test
  public void testManuallyInline() throws Exception {
    WebResource dbRoot = db.getDbRoot();

    {
      ClientResponse response = dbRoot
        .path( DOC_ID.asString() )
        .type( MediaType.APPLICATION_JSON_TYPE )
        .put( ClientResponse.class,
              getClass().getResourceAsStream( "doc_with_inlined_attachment_put.json" ) );

      assertEquals( 201, response.getStatus() );

      String doc = dbRoot.path( DOC_ID.asString() ).get( String.class );
      JsonUtils.assertJsonEquals( getClass().getResource( "doc_with_inlined_attachment_get.json" ), doc );
    }
  }

  @Test
  public void testManually() throws Exception {
    WebResource dbRoot = db.getDbRoot();

    {
      ClientResponse response = dbRoot.path( DOC_ID.asString() ).path( "test_data.xml" ).type( MediaType.APPLICATION_XML_TYPE ).put( ClientResponse.class, getClass().getResourceAsStream( "test_data.xml" ) );
      assertEquals( "{\"ok\":true,\"id\":\"daDocId\",\"rev\":\"" + REV_1 + "\"}", response.getEntity( String.class ).trim() );
      assertEquals( 201, response.getStatus() );

      String doc = dbRoot.path( DOC_ID.asString() ).get( String.class );
      JsonUtils.assertJsonEquals( getClass().getResource( "doc_with_attachment.json" ), doc );
    }

    {
      ClientResponse response = dbRoot.path( DOC_ID.asString() ).path( "test_data2.xml" ).queryParam( "rev", REV_1 ).type( MediaType.APPLICATION_XML_TYPE ).put( ClientResponse.class, getClass().getResourceAsStream( "test_data2.xml" ) );
      assertEquals( "{\"ok\":true,\"id\":\"daDocId\",\"rev\":\"" + REV_2 + "\"}", response.getEntity( String.class ).trim() );
      assertEquals( 201, response.getStatus() );

      String doc = dbRoot.path( DOC_ID.asString() ).get( String.class );
      JsonUtils.assertJsonEquals( getClass().getResource( "doc_with_attachment2.json" ), doc );
    }
  }

  @Test
  public void testDoc2() throws Exception {
    {
      ActionResponse response = db.put( DOC_ID, null, new AttachmentId( "test_data.xml" ), MediaType.APPLICATION_XML_TYPE, getClass().getResourceAsStream( "test_data.xml" ) );
      assertEquals( "daDocId", response.getId().asString() );
      assertEquals( REV_1, response.getRev().asString() );
    }

    //Add a second attachment
    {
      ActionResponse response = db.put( DOC_ID, new Revision( REV_1 ), new AttachmentId( "test_data2.xml" ), MediaType.APPLICATION_XML_TYPE, getClass().getResourceAsStream( "test_data2.xml" ) );
      assertEquals( "daDocId", response.getId().asString() );
      assertEquals( REV_2, response.getRev().asString() );
    }

    //Get the doc
    {
      byte[] read = ByteStreams.toByteArray( db.get( DOC_ID ) );
      JsonUtils.assertJsonEquals( getClass().getResource( "doc_with_attachment2.json" ), new String( read ) );
    }

    //Get the attachment1
    {
      byte[] read = ByteStreams.toByteArray( db.get( DOC_ID, new AttachmentId( "test_data.xml" ) ) );
      AssertUtils.assertXMLEquals( getClass().getResource( "test_data.xml" ), new String( read ) );
      assertEquals( new String( ByteStreams.toByteArray( getClass().getResourceAsStream( "test_data.xml" ) ) ), new String( read ) );
    }
    //Get the attachment2
    {
      byte[] read = ByteStreams.toByteArray( db.get( DOC_ID, new AttachmentId( "test_data2.xml" ) ) );
      AssertUtils.assertXMLEquals( getClass().getResource( "test_data2.xml" ), new String( read ) );
      assertEquals( new String( ByteStreams.toByteArray( getClass().getResourceAsStream( "test_data2.xml" ) ) ), new String( read ) );
    }

    //Update the attachment
    {
      ActionResponse response = db.put( DOC_ID, new Revision( REV_2 ), new AttachmentId( "test_data2.xml" ), MediaType.TEXT_PLAIN_TYPE, new ByteArrayInputStream( "newContent".getBytes() ) );
      assertEquals( "daDocId", response.getId().asString() );
      assertEquals( REV_3, response.getRev().asString() );

      byte[] read = ByteStreams.toByteArray( db.get( DOC_ID, new AttachmentId( "test_data2.xml" ) ) );
      assertEquals( "newContent", new String( read ) );
    }

    //Delete the attachment
    {
      ActionResponse response = db.delete( DOC_ID, new Revision( REV_3 ), new AttachmentId( "test_data.xml" ) );
      assertEquals( DOC_ID, response.getId() );
      assertEquals( REV_4, response.getRev().asString() );
    }

    {
      byte[] read = ByteStreams.toByteArray( db.get( DOC_ID ) );
      JsonUtils.assertJsonEquals( getClass().getResource( "doc_with_attachment_deleted.json" ), new String( read ) );
    }


    //Delete attachment with invalid rev
    {
      try {
        db.delete( DOC_ID, new Revision( REV_1 ), new AttachmentId( "test_data2.xml" ) );
        fail( "Where is the Exception" );
      } catch ( ActionFailedException e ) {
        assertEquals( "conflict", e.getError() );
        assertEquals( "Document update conflict.", e.getReason() );
      }
    }

  }
}
