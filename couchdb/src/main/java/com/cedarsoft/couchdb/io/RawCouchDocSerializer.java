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

import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.RawCouchDoc;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class RawCouchDocSerializer {
  @NonNls
  public static final String PROPERTY_ID = "_id";
  @NonNls
  public static final String PROPERTY_REV = "_rev";

  @NotNull
  public byte[] serialize( @NotNull RawCouchDoc info ) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serialize( info, out );
    return out.toByteArray();
  }

  public void serialize( @NotNull RawCouchDoc doc, @NotNull OutputStream out ) throws IOException {
    JsonGenerator generator = createJsonGenerator( out );
    try {
      serialize( doc, generator );
    } finally {
      generator.close();
    }
  }

  public void serialize( @NotNull RawCouchDoc doc, @NotNull JsonGenerator generator ) throws IOException {
    generator.writeStartObject();
    serializeIdAndRev( generator, doc );
    generator.writeEndObject();
  }

  @NotNull
  public RawCouchDoc deserialize( @NotNull InputStream in ) throws IOException {
    try {
      JsonParser parser = createJsonParser( in );
      RawCouchDoc doc = deserialize( parser );

      AbstractJacksonSerializer.ensureParserClosed( parser );
      return doc;
    } catch ( InvalidTypeException e ) {
      throw new IOException( "Could not parse due to " + e.getMessage(), e );
    }
  }

  @NotNull
  public RawCouchDoc deserialize( @NotNull JsonParser parser ) throws IOException, InvalidTypeException {
    AbstractJacksonSerializer.nextToken( parser, JsonToken.START_OBJECT );

    AbstractJacksonSerializer.nextFieldValue( parser, PROPERTY_ID );
    String id = parser.getText();

    AbstractJacksonSerializer.nextFieldValue( parser, PROPERTY_REV );
    String rev = parser.getText();

    parser.nextToken();

    AbstractJacksonSerializer.ensureObjectClosed( parser );
    return new RawCouchDoc( new DocId( id ), new Revision( rev ) );
  }

  public static void serializeIdAndRev( @NotNull JsonGenerator serializeTo, @NotNull RawCouchDoc doc ) throws IOException, JsonProcessingException {
    serializeTo.writeStringField( PROPERTY_ID, doc.getId().asString() );

    Revision rev = doc.getRev();
    if ( rev != null ) {
      serializeTo.writeStringField( PROPERTY_REV, rev.asString() );
    }
  }

  @NotNull
  protected static JsonParser createJsonParser( @NotNull InputStream in ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    return jsonFactory.createJsonParser( in );
  }

  @NotNull
  protected static JsonGenerator createJsonGenerator( @NotNull OutputStream out ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    return jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );
  }
}
