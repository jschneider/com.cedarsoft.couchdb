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

import com.cedarsoft.couchdb.core.ActionFailedException;
import com.cedarsoft.couchdb.core.ActionResponse;
import com.cedarsoft.couchdb.core.DocId;
import com.cedarsoft.couchdb.core.Revision;
import com.cedarsoft.couchdb.core.UniqueId;
import com.cedarsoft.serialization.jackson.JacksonParserWrapper;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import com.cedarsoft.version.VersionException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.io.input.TeeInputStream;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import javax.annotation.WillNotClose;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

public class ActionResponseSerializer {

  /**
   * Creates a new action response based on the given client response
   *
   * @param response the client response
   * @return the action response
   *
   * @throws ActionFailedException if there has been an error
   */
  @Nonnull
  public static ActionResponse create( @WillClose @Nonnull ClientResponse response ) throws ActionFailedException {
    try {
      verifyNoError( response );
      return new ActionResponseSerializer().deserialize( response );
    } finally {
      response.close();
    }
  }

  /**
   * Throws an exception if the response contains a value
   *
   * @param response the response
   * @throws ActionFailedException
   */
  public static void verifyNoError( @WillNotClose @Nonnull ClientResponse response ) throws ActionFailedException {
    if ( !ActionResponse.isNotSuccessful( response ) ) {
      return;
    }

    if ( !response.hasEntity() ) {
      throw new ActionFailedException( response.getStatus(), "unknown", "unknown", null );
    }

    try {
      try ( InputStream inputStream = response.getEntityInputStream() ) {
        throw new ActionFailedExceptionSerializer().deserialize( response.getStatus(), inputStream );
      }
    } catch ( IOException e ) {
      throw new RuntimeException( e );
    }
  }

  public static final String PROPERTY_ID = "id";
  public static final String PROPERTY_REV = "rev";
  public static final String PROPERTY_OK = "ok";

  @Nonnull
  public ActionResponse deserialize( @Nonnull ClientResponse response ) throws VersionException {
    if ( !MediaType.APPLICATION_JSON_TYPE.equals( response.getType() ) ) {
      throw new IllegalStateException( "Invalid media type: " + response.getType() );
    }

    InputStream entityInputStream = response.getEntityInputStream();

    try {
      //Wrap the input stream
      try ( MaxLengthByteArrayOutputStream teedOut = new MaxLengthByteArrayOutputStream(); TeeInputStream teeInputStream = new TeeInputStream( entityInputStream, teedOut ) ) {
        UniqueId uniqueId = deserialize( teeInputStream );

        return new ActionResponse( uniqueId, response.getStatus(), response.getLocation(), teedOut.toByteArray() );
      }
    } catch ( IOException e ) {
      throw new RuntimeException( e );
    }
  }

  @Nonnull
  public UniqueId deserialize( @Nonnull InputStream in ) throws VersionException, IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();

    JsonParser parser = jsonFactory.createJsonParser( in );
    JacksonParserWrapper parserWrapper = new JacksonParserWrapper( parser );
    parserWrapper.nextToken( JsonToken.START_OBJECT );

    UniqueId deserialized = deserialize( parser );

    parserWrapper.ensureObjectClosed();

    return deserialized;
  }

  @Nonnull
  public UniqueId deserialize( @Nonnull JsonParser deserializeFrom ) throws VersionException, IOException {
    JacksonParserWrapper parser = new JacksonParserWrapper( deserializeFrom );

    String id = null;
    String rev = null;

    while ( parser.nextToken() == JsonToken.FIELD_NAME ) {
      String currentName = parser.getCurrentName();

      if ( currentName.equals( PROPERTY_OK ) ) {
        parser.nextToken( JsonToken.VALUE_TRUE );
        //we don't need that value
        continue;
      }

      if ( currentName.equals( PROPERTY_ID ) ) {
        parser.nextToken( JsonToken.VALUE_STRING );
        id = deserializeFrom.getText();
        continue;
      }

      if ( currentName.equals( PROPERTY_REV ) ) {
        parser.nextToken( JsonToken.VALUE_STRING );
        rev = deserializeFrom.getText();
        continue;
      }

      throw new IllegalStateException( "Unexpected field reached <" + currentName + ">" );
    }

    parser.verifyDeserialized( id, PROPERTY_ID );
    parser.verifyDeserialized( rev, PROPERTY_REV );
    assert rev != null;
    assert id != null;

    parser.ensureObjectClosed();

    return new UniqueId( new DocId( id ), new Revision( rev ) );

    //    AbstractJacksonSerializer.nextToken( deserializeFrom, JsonToken.FIELD_NAME );
    //    if ( deserializeFrom.getCurrentName().equals( PROPERTY_OK ) ) {
    //    } else {
    //      AbstractJacksonSerializer.nextToken( deserializeFrom, JsonToken.VALUE_STRING );
    //      String error = deserializeFrom.getText();
    //      AbstractJacksonSerializer.nextFieldValue( deserializeFrom, PROPERTY_REASON );
    //      String reason = deserializeFrom.getText();
    //      AbstractJacksonSerializer.closeObject( deserializeFrom );
    //      return new ActionResponse( new ActionResponse.Error( error, reason ) );
    //    }
  }
}
