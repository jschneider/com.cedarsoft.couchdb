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

import com.cedarsoft.serialization.jackson.JacksonParserWrapper;
import com.cedarsoft.version.VersionException;
import com.cedarsoft.couchdb.core.ActionFailedException;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.io.input.TeeInputStream;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class ActionFailedExceptionSerializer {
  @Nonnull
  public static final String PROPERTY_ERROR = "error";
  @Nonnull
  public static final String PROPERTY_REASON = "reason";

  public void serialize( @Nonnull ActionFailedException object, @Nonnull OutputStream out ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    JsonGenerator generator = jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );

    generator.writeStartObject();

    serialize( generator, object );
    generator.writeEndObject();

    generator.close();
  }

  @Nonnull
  public ActionFailedException deserialize( int status, @Nonnull InputStream in ) throws VersionException, IOException {
    try ( MaxLengthByteArrayOutputStream teedOut = new MaxLengthByteArrayOutputStream(); TeeInputStream teeInputStream = new TeeInputStream( in, teedOut ) ) {

      JsonFactory jsonFactory = JacksonSupport.getJsonFactory();

      JsonParser parser = jsonFactory.createJsonParser( teeInputStream );
      JacksonParserWrapper parserWrapper = new JacksonParserWrapper( parser );

      parserWrapper.nextToken(  JsonToken.START_OBJECT );

      parserWrapper.nextFieldValue(  PROPERTY_ERROR );
      String error = parser.getText();
      parserWrapper.nextFieldValue(  PROPERTY_REASON );
      String reason = parser.getText();
      parserWrapper.closeObject();
      parserWrapper.ensureObjectClosed();

      return new ActionFailedException( status, error, reason, teedOut.toByteArray() );
    }
  }

  public void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull ActionFailedException object ) throws IOException {
    serializeTo.writeStringField( PROPERTY_ERROR, object.getError() );
    serializeTo.writeStringField( PROPERTY_REASON, object.getReason() );
  }

}
