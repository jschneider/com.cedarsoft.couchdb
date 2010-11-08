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

import com.cedarsoft.Version;
import com.cedarsoft.couchdb.CouchDoc;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.JacksonSerializer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CouchDocSerializer extends RawCouchDocSerializer {
  @NotNull
  public <T> byte[] serialize( @NotNull CouchDoc<T> doc, @NotNull JacksonSerializer<? super T> wrappedSerializer ) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serialize( doc, wrappedSerializer, out );
    return out.toByteArray();
  }

  public <T> void serialize( @NotNull CouchDoc<T> doc, @NotNull JacksonSerializer<? super T> wrappedSerializer, @NotNull OutputStream out ) throws IOException {
    JsonGenerator generator = createJsonGenerator( out );

    serialize( doc, wrappedSerializer, generator );
    generator.close();
  }

  public <T> void serialize( @NotNull CouchDoc<T> doc, @NotNull JacksonSerializer<? super T> wrappedSerializer, @NotNull JsonGenerator generator ) throws IOException {
    generator.writeStartObject();
    serializeIdAndRev( generator, doc );

    //Type
    generator.writeStringField( AbstractJacksonSerializer.PROPERTY_TYPE, wrappedSerializer.getType() );
    //Version
    generator.writeStringField( AbstractJacksonSerializer.PROPERTY_VERSION, wrappedSerializer.getFormatVersion().format() );

    //The wrapped object
    wrappedSerializer.serialize( generator, doc.getObject(), wrappedSerializer.getFormatVersion() );

    generator.writeEndObject();
  }

  @NotNull
  public <T> CouchDoc<T> deserialize( @NotNull JacksonSerializer<T> wrappedSerializer, @NotNull InputStream in ) throws IOException {
    try {
      JsonParser parser = createJsonParser( in );
      CouchDoc<T> doc = deserialize( wrappedSerializer, parser );

      AbstractJacksonSerializer.ensureParserClosed( parser );
      return doc;
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
    return new CouchDoc<T>( new DocId( id ), rev == null ? null : new Revision( rev ), wrapped );
  }
}
