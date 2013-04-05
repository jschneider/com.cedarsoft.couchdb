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

import com.cedarsoft.couchdb.CouchDoc;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.Row;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.JacksonParserWrapper;
import com.cedarsoft.serialization.jackson.JacksonSerializer;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class RowSerializer {

  public static final String PROPERTY_ID = "id";

  public static final String PROPERTY_KEY = "key";

  public static final String PROPERTY_VALUE = "value";

  @Nonnull
  private final CouchDocSerializer couchDocSerializer;

  @Deprecated
  public RowSerializer() {
    this( new CouchDocSerializer() );
  }

  public RowSerializer( @Nonnull CouchDocSerializer couchDocSerializer ) {
    this.couchDocSerializer = couchDocSerializer;
  }

  public <K, V> void serialize( @Nonnull Row<K, V, ?> row, @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nonnull OutputStream out ) throws IOException {
    serialize( row, keySerializer, valueSerializer, null, out );
  }

  public <K, V> void serialize( @Nonnull Row<K, V, ?> row, @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nonnull JsonGenerator generator ) throws IOException {
    serialize( row, keySerializer, valueSerializer, null, generator );
  }

  public <K, V, D> void serialize( @Nonnull Row<K, V, D> row, @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nullable JacksonSerializer<? super D> documentSerializer, @Nonnull OutputStream out ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    JsonGenerator generator = jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );

    serialize( row, keySerializer, valueSerializer, documentSerializer, generator );
    generator.close();
  }

  public <K, V, D> void serialize( @Nonnull Row<K, V, D> row, @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nullable JacksonSerializer<? super D> documentSerializer, @Nonnull JsonGenerator generator ) throws IOException {
    generator.writeStartObject();

    @Nullable DocId id = row.getId();
    if ( id != null ) {
      generator.writeStringField( PROPERTY_ID, id.asString() );
    }

    //The key
    generator.writeFieldName( PROPERTY_KEY );
    keySerializer.serialize( row.getKey(), generator );

    //The Value
    generator.writeFieldName( PROPERTY_VALUE );
    V value = row.getValue();
    if ( value == null ) {
      generator.writeNull();
    } else {
      valueSerializer.serialize( value, generator );
    }

    //The doc
    CouchDoc<? extends D> doc = row.getDoc();
    if ( doc != null ) {
      if ( documentSerializer == null ) {
        throw new NullPointerException( "documentSerializer must not be null when serializing a doc" );
      }

      generator.writeFieldName( "doc" );
      couchDocSerializer.serialize( doc, documentSerializer, generator );
    }

    generator.writeEndObject();
  }

  @Nonnull
  public <K, V> Row<K, V, Void> deserialize( @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nonnull InputStream in ) throws IOException, InvalidTypeException {
    return deserialize( keySerializer, valueSerializer, null, in );
  }

  @Nonnull
  public <K, V, D> Row<K, V, D> deserialize( @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nullable JacksonSerializer<? extends D> documentSerializer, @Nonnull InputStream in ) throws IOException, InvalidTypeException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();

    JsonParser parser = jsonFactory.createJsonParser( in );
    AbstractJacksonSerializer.nextToken( parser, JsonToken.START_OBJECT );

    return deserialize( keySerializer, valueSerializer, documentSerializer, parser );
  }

  @Nonnull
  public <K, V> Row<K, V, Void> deserialize( @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nonnull JsonParser parser ) throws IOException, InvalidTypeException {
    return deserialize( keySerializer, valueSerializer, null, parser );
  }

  @Nonnull
  public <K, V, D> Row<K, V, D> deserialize( @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nullable JacksonSerializer<? extends D> documentSerializer, @Nonnull JsonParser parser ) throws IOException, InvalidTypeException {
    JacksonParserWrapper wrapper = new JacksonParserWrapper( parser );
    wrapper.nextToken( JsonToken.FIELD_NAME );
    String fieldName = wrapper.getCurrentName();

    //The id
    @Nullable final DocId id;
    if ( fieldName.equals( PROPERTY_ID ) ) {
      wrapper.nextValue();
      id = new DocId( wrapper.getText() );
      wrapper.nextField( PROPERTY_KEY );
    }else {
      id = null;
    }

    //The key
    K key = ( K ) keySerializer.deserialize( parser );

    //The value
    wrapper.nextField( PROPERTY_VALUE );

    @Nullable
    V value = ( V ) valueSerializer.deserialize( parser );

    //The doc - if available
    @Nullable
    CouchDoc<? extends D> doc;

    JsonToken nextToken = wrapper.nextToken();
    if ( nextToken == JsonToken.FIELD_NAME ) {
      if ( documentSerializer == null ) {
        throw new NullPointerException( "No document serializer found" );
      }
      doc = couchDocSerializer.deserialize( documentSerializer, new JacksonParserWrapper( parser ) );

      wrapper.closeObject();
    } else {
      doc = null;
    }

    wrapper.verifyCurrentToken( JsonToken.END_OBJECT );
    return new Row<>( id, key, value, doc );
  }
}
