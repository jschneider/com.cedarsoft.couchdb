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

import com.cedarsoft.couchdb.core.AttachmentId;
import com.cedarsoft.couchdb.core.CouchDoc;
import com.cedarsoft.couchdb.core.DocId;
import com.cedarsoft.couchdb.core.Revision;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.JacksonParserWrapper;
import com.cedarsoft.serialization.jackson.JacksonSerializer;
import com.cedarsoft.version.Version;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.sun.jersey.core.util.Base64;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CouchDocSerializer {

  public static final String PROPERTY_ID = RawCouchDocSerializer.PROPERTY_ID;

  public static final String PROPERTY_REV = RawCouchDocSerializer.PROPERTY_REV;
  public static final String PROPERTY_ATTACHMENTS = "_attachments";
  public static final String PROPERTY_CONTENT_TYPE = "content_type";
  public static final String PROPERTY_DATA = "data";

  @Nonnull
  public <T> byte[] serialize( @Nonnull CouchDoc<T> doc, @Nonnull JacksonSerializer<? super T> wrappedSerializer ) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serialize( doc, wrappedSerializer, out );
    return out.toByteArray();
  }

  public <T> void serialize( @Nonnull CouchDoc<T> doc, @Nonnull JacksonSerializer<? super T> wrappedSerializer, @Nonnull OutputStream out ) throws IOException {
    JsonGenerator generator = RawCouchDocSerializer.createJsonGenerator( out );

    serialize( doc, wrappedSerializer, generator );
    generator.close();
  }

  public <T> void serialize( @Nonnull CouchDoc<T> doc, @Nonnull JacksonSerializer<? super T> wrappedSerializer, @Nonnull JsonGenerator generator ) throws IOException {
    generator.writeStartObject();
    RawCouchDocSerializer.serializeIdAndRev( generator, doc );

    //Type
    generator.writeStringField( AbstractJacksonSerializer.PROPERTY_TYPE, wrappedSerializer.getType() );
    //Version
    generator.writeStringField( AbstractJacksonSerializer.PROPERTY_VERSION, wrappedSerializer.getFormatVersion().format() );

    //The wrapped object
    wrappedSerializer.serialize( generator, doc.getObject(), wrappedSerializer.getFormatVersion() );

    //The attachments - placed at the end
    serializeInlineAttachments( doc, generator );

    generator.writeEndObject();
  }

  private static <T> void serializeInlineAttachments( @Nonnull CouchDoc<T> doc, @Nonnull JsonGenerator generator ) throws IOException {
    if ( !doc.hasInlineAttachments() ) {
      return;
    }

    generator.writeObjectFieldStart( PROPERTY_ATTACHMENTS );

    for ( CouchDoc.Attachment attachment : doc.getAttachments() ) {
      if ( !attachment.isInline() ) {
        throw new IllegalStateException( "Cannot serialize non-inline attachments: " + attachment );
      }
      generator.writeObjectFieldStart( attachment.getId().asString() );

      generator.writeStringField( PROPERTY_CONTENT_TYPE, attachment.getContentType().toString() );
      generator.writeStringField( PROPERTY_DATA, new String( Base64.encode( attachment.getData() ) ) );

      generator.writeEndObject();
    }

    generator.writeEndObject();
  }

  @Nonnull
  public <T> CouchDoc<T> deserialize( @Nonnull JacksonSerializer<T> wrappedSerializer, @WillClose @Nonnull InputStream in ) {
    try {
      try {
        JsonParser parser = RawCouchDocSerializer.createJsonParser( in );
        JacksonParserWrapper parserWrapper = new JacksonParserWrapper( parser );
        CouchDoc<T> doc = deserialize( wrappedSerializer, parserWrapper );

        parserWrapper.ensureParserClosed();
        return doc;
      } finally {
        in.close();
      }
    } catch ( IOException e ) {
      throw new RuntimeException( "Could not parse due to " + e.getMessage(), e );
    }
  }

  @Nonnull
  public <T> CouchDoc<T> deserialize( @Nonnull JacksonSerializer<T> wrappedSerializer, @Nonnull JacksonParserWrapper parserWrapper ) throws IOException {
    parserWrapper.nextToken( JsonToken.START_OBJECT );

    parserWrapper.nextFieldValue( PROPERTY_ID );
    String id = parserWrapper.getText();

    parserWrapper.nextFieldValue( PROPERTY_REV );
    String rev = parserWrapper.getText();

    //Type and Version
    parserWrapper.nextFieldValue( AbstractJacksonSerializer.PROPERTY_TYPE );
    wrappedSerializer.verifyType( parserWrapper.getText() );
    parserWrapper.nextFieldValue( AbstractJacksonSerializer.PROPERTY_VERSION );
    Version version = Version.parse( parserWrapper.getText() );

    //The wrapped object
    T wrapped = wrappedSerializer.deserialize( parserWrapper.getParser(), version );

    //the attachments - if there are any....
    List<? extends CouchDoc.Attachment> attachments = deserializeAttachments( parserWrapper );

    parserWrapper.ensureObjectClosed();
    CouchDoc<T> doc = new CouchDoc<>( new DocId( id ), rev == null ? null : new Revision( rev ), wrapped );
    doc.addAttachments( attachments );
    return doc;
  }

  @Nonnull
  private static List<? extends CouchDoc.Attachment> deserializeAttachments( @Nonnull JacksonParserWrapper parserWrapper ) throws IOException {
    List<CouchDoc.Attachment> attachments = new ArrayList<>();

    //check for attachments
    if ( parserWrapper.getCurrentToken() == JsonToken.FIELD_NAME && parserWrapper.getCurrentName().equals( PROPERTY_ATTACHMENTS ) ) {
      parserWrapper.nextToken( JsonToken.START_OBJECT );

      while ( parserWrapper.nextToken() != JsonToken.END_OBJECT ) {
        String attachmentId = parserWrapper.getCurrentName();

        parserWrapper.nextToken( JsonToken.START_OBJECT );
        parserWrapper.nextFieldValue( PROPERTY_CONTENT_TYPE );
        String contentType = parserWrapper.getText();
        parserWrapper.nextFieldValue( "revpos" );
        parserWrapper.nextFieldValue( "digest" );
        parserWrapper.nextFieldValue( "length" );
        long length = parserWrapper.getNumberValue().longValue();
        parserWrapper.nextFieldValue( "stub" );

        attachments.add( new CouchDoc.StubbedAttachment( new AttachmentId( attachmentId ), MediaType.valueOf( contentType ), length ) );

        parserWrapper.nextToken( JsonToken.END_OBJECT );
      }
      parserWrapper.nextToken( JsonToken.END_OBJECT );
    }

    return attachments;
  }
}
