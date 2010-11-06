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

import com.cedarsoft.VersionException;
import com.cedarsoft.couchdb.CreationResponse;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreationResponseSerializer {
  @NonNls
  public static final String PROPERTY_ID = "id";
  @NonNls
  public static final String PROPERTY_REV = "rev";
  @NonNls
  public static final String PROPERTY_ERROR = "error";
  @NonNls
  public static final String PROPERTY_REASON = "reason";
  @NonNls
  public static final String PROPERTY_OK = "ok";


  public void serialize( @NotNull CreationResponse object, @NotNull OutputStream out ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();

    JsonGenerator generator = jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );

    generator.writeStartObject();

    serialize( generator, object );
    generator.writeEndObject();

    generator.close();
  }

  @NotNull
  public CreationResponse deserialize( @NotNull InputStream in ) throws IOException, VersionException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();

    JsonParser parser = jsonFactory.createJsonParser( in );
    AbstractJacksonSerializer.nextToken( parser, JsonToken.START_OBJECT );

    CreationResponse deserialized = deserialize( parser );

    AbstractJacksonSerializer.ensureParserClosedObject( parser );

    return deserialized;
  }

  public void serialize( @NotNull JsonGenerator serializeTo, @NotNull CreationResponse object ) throws IOException, JsonProcessingException {
    serializeTo.writeBooleanField( PROPERTY_OK, true );
    serializeTo.writeStringField( PROPERTY_ID, object.getId().asString() );
    serializeTo.writeStringField( PROPERTY_REV, object.getRev().asString() );

    //    if ( object.isSuccess() ) {
    //    } else {
    //      serializeTo.writeStringField( PROPERTY_ERROR, object.asError().getError() );
    //      serializeTo.writeStringField( PROPERTY_REASON, object.asError().getReason() );
    //    }
  }

  @NotNull
  public CreationResponse deserialize( @NotNull JsonParser deserializeFrom ) throws VersionException, IOException, JsonProcessingException {
    AbstractJacksonSerializer.nextFieldValue( deserializeFrom, PROPERTY_OK );
    AbstractJacksonSerializer.nextFieldValue( deserializeFrom, PROPERTY_ID );
    String id = deserializeFrom.getText();
    AbstractJacksonSerializer.nextFieldValue( deserializeFrom, PROPERTY_REV );
    String rev = deserializeFrom.getText();
    AbstractJacksonSerializer.closeObject( deserializeFrom );
    return new CreationResponse( new DocId( id ), new Revision( rev ) );

    //    AbstractJacksonSerializer.nextToken( deserializeFrom, JsonToken.FIELD_NAME );
    //    if ( deserializeFrom.getCurrentName().equals( PROPERTY_OK ) ) {
    //    } else {
    //      AbstractJacksonSerializer.nextToken( deserializeFrom, JsonToken.VALUE_STRING );
    //      String error = deserializeFrom.getText();
    //      AbstractJacksonSerializer.nextFieldValue( deserializeFrom, PROPERTY_REASON );
    //      String reason = deserializeFrom.getText();
    //      AbstractJacksonSerializer.closeObject( deserializeFrom );
    //      return new CreationResponse( new CreationResponse.Error( error, reason ) );
    //    }
  }
}
