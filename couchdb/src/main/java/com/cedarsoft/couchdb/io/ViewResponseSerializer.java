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

import com.cedarsoft.couchdb.core.Row;
import com.cedarsoft.couchdb.core.ViewResponse;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.JacksonParserWrapper;
import com.cedarsoft.serialization.jackson.JacksonSerializer;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ViewResponseSerializer {

  public static final String PROPERTY_TOTAL_ROWS = "total_rows";

  public static final String PROPERTY_OFFSET = "offset";

  public static final String PROPERTY_ROWS = "rows";

  @Nonnull
  private final RowSerializer rowSerializer;

  public ViewResponseSerializer( @Nonnull RowSerializer rowSerializer ) {
    this.rowSerializer = rowSerializer;
  }

  public <K, V> void serialize( @Nonnull ViewResponse<K, V, ?> viewResponse, @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nonnull OutputStream out ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    JsonGenerator generator = jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );

    generator.writeStartObject();

    generator.writeNumberField( PROPERTY_TOTAL_ROWS, viewResponse.getTotalRows() );
    generator.writeNumberField( PROPERTY_OFFSET, viewResponse.getOffset() );

    //Now the rows
    generator.writeFieldName( PROPERTY_ROWS );
    generator.writeStartArray();


    for ( Row<K, V, ?> row : viewResponse.getRows() ) {
      rowSerializer.serialize( row, keySerializer, valueSerializer, generator );
    }

    generator.writeEndArray();

    generator.writeEndObject();
    generator.close();
  }

  @Nonnull
  public <K, V> ViewResponse<K, V, Void> deserialize( @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nonnull InputStream in ) throws IOException, InvalidTypeException {
    return deserialize( keySerializer, valueSerializer, null, in );
  }

  public <K, V, D> ViewResponse<K, V, D> deserialize( @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nullable JacksonSerializer<? extends D> documentSerializer, @Nonnull InputStream in ) throws IOException, InvalidTypeException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    JsonParser parser = jsonFactory.createJsonParser( in );

    JacksonParserWrapper parserWrapper = new JacksonParserWrapper( parser );
    parserWrapper.nextToken(  JsonToken.START_OBJECT );


    parserWrapper.nextToken(  JsonToken.FIELD_NAME );
    //If reduced, no total rows and no offset are availlable!

    String fieldName = parser.getText();

    int totalRows = -1;
    int offset = -1;
    while ( !fieldName.equals( PROPERTY_ROWS ) ) {
      if ( fieldName.equals( PROPERTY_TOTAL_ROWS ) ) {
        parserWrapper.nextToken( JsonToken.VALUE_NUMBER_INT );
        totalRows = parser.getIntValue();
      }

      if ( fieldName.equals( PROPERTY_OFFSET ) ) {
        parserWrapper.nextToken( JsonToken.VALUE_NUMBER_INT );
        offset = parser.getIntValue();
      }

      parserWrapper.nextToken( JsonToken.FIELD_NAME );
      fieldName = parser.getText();
    }

    //Now the rows...
    parserWrapper.nextToken( JsonToken.START_ARRAY );

    List<Row<K, V, D>> deserialized = new ArrayList<>();
    while ( parser.nextToken() != JsonToken.END_ARRAY ) {
      Row<K, V, D> deserializedRow = rowSerializer.deserialize( keySerializer, valueSerializer, documentSerializer, parser );
      deserialized.add( deserializedRow );
    }

    return new ViewResponse<>( totalRows, offset, deserialized );
  }
}
