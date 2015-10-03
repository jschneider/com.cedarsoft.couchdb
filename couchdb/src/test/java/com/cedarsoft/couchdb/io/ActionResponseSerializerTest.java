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

import com.cedarsoft.couchdb.core.ActionResponse;
import com.cedarsoft.couchdb.core.DocId;
import com.cedarsoft.couchdb.core.Revision;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import com.cedarsoft.serialization.test.utils.AbstractSerializerTest2;
import com.cedarsoft.serialization.test.utils.Entry;
import com.cedarsoft.test.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.InBoundHeaders;
import org.junit.experimental.theories.*;
import org.junit.runner.*;

import javax.annotation.Nonnull;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.*;


@RunWith( Theories.class )
public class ActionResponseSerializerTest {
  @DataPoint
  public static Entry<? extends ActionResponse> success() throws URISyntaxException {
    return AbstractSerializerTest2.create(
      new ActionResponse( new DocId( "daid" ), new Revision( "darev" ), 200, new URI( "asdf" ) ),
      ActionResponseSerializerTest.class.getResource( "ActionResponse.json" )
    );
  }

  @Theory
  public void testEntry( Entry<? extends ActionResponse> entry ) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serialize( entry.getObject(), out );

    JsonUtils.assertJsonEquals( new String( entry.getExpected(), Charsets.UTF_8 ), new String( out.toByteArray(), Charsets.UTF_8 ) );

    ClientResponse clientResponse = new ClientResponse( 200, new InBoundHeaders(), new ByteArrayInputStream( entry.getExpected() ), null ){
      @Override
      public MediaType getType() {
        return MediaType.APPLICATION_JSON_TYPE;
      }
    };

    ActionResponse response = new ActionResponseSerializer().deserialize( clientResponse );
    assertThat( response ).isNotNull();
    assertThat( response.getRaw() ).isNotNull();
    JsonUtils.assertJsonEquals( new String( entry.getExpected(), Charsets.UTF_8 ), new String( response.getRaw(), Charsets.UTF_8 ) );
  }

  /**
   * Only used for tests
   * @param object
   * @param out
   * @throws IOException
   */
  @Deprecated
  public static void serialize( @Nonnull ActionResponse object, @Nonnull OutputStream out ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();

    JsonGenerator generator = jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );

    generator.writeStartObject();

    serialize( generator, object );
    generator.writeEndObject();

    generator.close();
  }

  /**
   * This is only a helper method used for tests
   * @param serializeTo
   * @param object
   * @throws IOException
   * @throws JsonProcessingException
   */
  @Deprecated
  public static void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull ActionResponse object ) throws IOException, JsonProcessingException {
    serializeTo.writeBooleanField( ActionResponseSerializer.PROPERTY_OK, true );
    serializeTo.writeStringField( ActionResponseSerializer.PROPERTY_ID, object.getId().asString() );
    serializeTo.writeStringField( ActionResponseSerializer.PROPERTY_REV, object.getRev().asString() );

    //    if ( object.isSuccess() ) {
    //    } else {
    //      serializeTo.writeStringField( PROPERTY_ERROR, object.asError().getError() );
    //      serializeTo.writeStringField( PROPERTY_REASON, object.asError().getReason() );
    //    }
  }
}
