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
package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.core.DocId;
import com.cedarsoft.couchdb.core.Revision;
import com.cedarsoft.couchdb.io.CouchSerializerWrapper;
import com.cedarsoft.test.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DocWrappingSerializerTest {
  private URL res;

  @Before
  public void setUp() throws Exception {
    res = getClass().getResource( "bar.json" );
  }

  @Test
  public void testSerialize() throws Exception {
    CouchSerializerWrapper<Bar> serializer = new CouchSerializerWrapper<Bar>( new Bar.Serializer() );

    Bar bar = new Bar( 123, "asdf" );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      serializer.serialize( bar, out );
      fail( "Where is the Exception" );
    } catch ( UnsupportedOperationException e ) {
      assertThat( e ).hasMessage( "Use #serialize(Object, OutputStream, DocId, Revision) instead" );
    }

    serializer.serialize( bar, out, new DocId( "dadocid" ), new Revision( "darevision" ) );

    JsonUtils.assertJsonEquals( "{\n" +
                                  "  \"_id\" : \"dadocid\",\n" +
                                  "  \"_rev\" : \"darevision\",\n" +
                                  "  \"@type\" : \"bar\",\n" +
                                  "  \"@version\" : \"1.0.0\",\n" +
                                  "  \"value\" : 123,\n" +
                                  "  \"description\" : \"asdf\"\n" +
                                  "}", new String( out.toByteArray() ) );
  }

  @Test
  public void testException() throws Exception {
    try {
      new Bar.Serializer().deserialize( res.openStream() );
      fail( "Where is the Exception" );
    } catch ( JsonParseException e ) {
      assertThat( e.getMessage() ).startsWith( "Invalid field. Expected <@type> but was <_id>" );
    }
  }

  @Test
  public void testMulti() throws Exception {
    CouchSerializerWrapper<Bar> serializer = new CouchSerializerWrapper<Bar>( new Bar.Serializer() );
    serializer.deserialize( res.openStream() );
    serializer.deserialize( res.openStream() );
    serializer.deserialize( res.openStream() );
    serializer.deserialize( res.openStream() );
    serializer.deserialize( res.openStream() );
    serializer.deserialize( res.openStream() );
  }

  @Test
  public void testWrapped() throws Exception {
    CouchSerializerWrapper<Bar> serializer = new CouchSerializerWrapper<Bar>( new Bar.Serializer() );
    InputStream in = res.openStream();
    Bar bar = serializer.deserialize( in );

    assertThat( bar.getValue() ).isEqualTo( 7 );
    assertThat( bar.getDescription() ).isEqualTo( "hey" );
  }

  @Test
  public void testWrapped1() throws Exception {
    runWrapped( 1 );
  }

  @Test
  public void testWrapped2() throws Exception {
    runWrapped( 2 );
  }

  @Test
  public void testWrapped3() throws Exception {
    runWrapped( 3 );
  }

  @Test
  public void testWrapped4() throws Exception {
    runWrapped( 4 );
  }

  private void runWrapped( int i ) throws IOException {
    CouchSerializerWrapper<Bar> serializer = new CouchSerializerWrapper<>( new Bar.Serializer() );

    try ( InputStream in = getClass().getResourceAsStream( "wrapped" + i + ".json" ) ) {
      Bar bar = serializer.deserialize( in );
      assertThat( bar.getValue() ).isEqualTo( 123 );
      assertThat( bar.getDescription() ).isEqualTo( "descr" );
    }
  }
}
