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

import com.cedarsoft.Version;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.JacksonSerializer;
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

public class CouchDocSerializer {
  @NonNls
  public static final String PROPERTY_ID = "_id";
  @NonNls
  public static final String PROPERTY_REV = "_rev";

  @NotNull
  public <T> byte[] serialize( @NotNull CouchDoc<T> info, @NotNull JacksonSerializer<? super T> wrappedSerializer ) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serialize( info, wrappedSerializer, out );
    return out.toByteArray();
  }

  public <T> void serialize( @NotNull CouchDoc<T> info, @NotNull JacksonSerializer<? super T> wrappedSerializer, @NotNull OutputStream out ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    JsonGenerator generator = jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );

    serialize( info, wrappedSerializer, generator );
    generator.close();
  }

  public <T> void serialize( @NotNull CouchDoc<T> info, @NotNull JacksonSerializer<? super T> wrappedSerializer, @NotNull JsonGenerator generator ) throws IOException {
    generator.writeStartObject();
    serializeIdAndRev( generator, info );

    //Type
    generator.writeStringField( AbstractJacksonSerializer.PROPERTY_TYPE, wrappedSerializer.getType() );
    //Version
    generator.writeStringField( AbstractJacksonSerializer.PROPERTY_VERSION, wrappedSerializer.getFormatVersion().format() );

    //The wrapped object
    wrappedSerializer.serialize( generator, info.getObject(), wrappedSerializer.getFormatVersion() );

    generator.writeEndObject();
  }

  public void serializeIdAndRev( @NotNull JsonGenerator serializeTo, @NotNull CouchDoc<?> object ) throws IOException, JsonProcessingException {
    serializeTo.writeStringField( PROPERTY_ID, object.getId() );

    String rev = object.getRev();
    if ( rev != null ) {
      serializeTo.writeStringField( PROPERTY_REV, rev );
    }
  }

  @NotNull
  public <T> CouchDoc<T> deserialize( @NotNull JacksonSerializer<T> wrappedSerializer, @NotNull InputStream in ) throws IOException {
    try {
      JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
      JsonParser parser = jsonFactory.createJsonParser( in );
      CouchDoc<T> info = deserialize( wrappedSerializer, parser );

      AbstractJacksonSerializer.ensureParserClosed( parser );
      return info;
    } catch ( InvalidTypeException e ) {
      throw new IOException( "Could not parse due to " + e.getMessage(), e );
    }
  }

  @NotNull
  public <T> CouchDoc<T> deserialize( @NotNull JacksonSerializer<T> wrappedSerializer, @NotNull JsonParser parser ) throws IOException, InvalidTypeException {
    AbstractJacksonSerializer.nextToken( parser, JsonToken.START_OBJECT );

    AbstractJacksonSerializer.nextFieldValue( parser, PROPERTY_ID );
    String id = parser.getText();

    AbstractJacksonSerializer.nextFieldValue( parser, PROPERTY_REV );
    String rev = parser.getText();


    AbstractJacksonSerializer.nextFieldValue( parser, AbstractJacksonSerializer.PROPERTY_TYPE );
    wrappedSerializer.verifyType( parser.getText() );
    AbstractJacksonSerializer.nextFieldValue( parser, AbstractJacksonSerializer.PROPERTY_VERSION );
    Version version = Version.parse( parser.getText() );

    T wrapped = wrappedSerializer.deserialize( parser, version );

    AbstractJacksonSerializer.ensureObjectClosed( parser );
    return new CouchDoc<T>( id, rev, wrapped );
  }
}
