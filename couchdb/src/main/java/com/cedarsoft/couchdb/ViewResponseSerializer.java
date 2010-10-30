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

import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.JacksonSerializer;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ViewResponseSerializer {
  @NonNls
  public static final String PROPERTY_TOTAL_ROWS = "total_rows";
  @NonNls
  public static final String PROPERTY_OFFSET = "offset";
  @NonNls
  public static final String PROPERTY_ROWS = "rows";

  @NotNull
  private final RowSerializer rowSerializer;

  public ViewResponseSerializer( @NotNull RowSerializer rowSerializer ) {
    this.rowSerializer = rowSerializer;
  }

  public <K, V> void serialize( @NotNull ViewResponse<K, V, ?> viewResponse, @NotNull JacksonSerializer<? super K> keySerializer, @NotNull JacksonSerializer<? super V> valueSerializer, @NotNull OutputStream out ) throws IOException {
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

  @NotNull
  public <K, V> ViewResponse<K, V, Void> deserialize( @NotNull JacksonSerializer<? super K> keySerializer, @NotNull JacksonSerializer<? super V> valueSerializer, @NotNull InputStream in ) throws IOException, InvalidTypeException {
    return deserialize( keySerializer, valueSerializer, null, in );
  }

  public <K, V, D> ViewResponse<K, V, D> deserialize( @NotNull JacksonSerializer<? super K> keySerializer, @NotNull JacksonSerializer<? super V> valueSerializer, @Nullable JacksonSerializer<? extends D> documentSerializer, @NotNull InputStream in ) throws IOException, InvalidTypeException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    JsonParser parser = jsonFactory.createJsonParser( in );

    AbstractJacksonSerializer.nextToken( parser, JsonToken.START_OBJECT );

    AbstractJacksonSerializer.nextFieldValue( parser, PROPERTY_TOTAL_ROWS );
    int totalRows = parser.getIntValue();

    AbstractJacksonSerializer.nextFieldValue( parser, PROPERTY_OFFSET );
    int offset = parser.getIntValue();


    AbstractJacksonSerializer.nextFieldValue( parser, PROPERTY_ROWS );

    List<Row<K, V, D>> deserialized = new ArrayList<Row<K, V, D>>();
    while ( parser.nextToken() != JsonToken.END_ARRAY ) {
      Row<K, V, D> deserializedRow = rowSerializer.deserialize( keySerializer, valueSerializer, documentSerializer, parser );
      deserialized.add( deserializedRow );
    }

    return new ViewResponse<K, V, D>( totalRows, offset, deserialized );
  }
}
