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

import com.cedarsoft.couchdb.io.CouchSerializerWrapper;
import com.cedarsoft.version.VersionException;
import org.codehaus.jackson.JsonParseException;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

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
  public void testException() throws Exception {
    try {
      new Bar.Serializer().deserialize( res.openStream() );
      fail( "Where is the Exception" );
    } catch ( JsonParseException e ) {
      assertThat( e.getMessage() ).startsWith( "Invalid field. Expected <@type> but was <_id>" );
    }
  }

  @Test
  public void testWrapped() throws Exception {
    CouchSerializerWrapper<Bar> serializer = new CouchSerializerWrapper<Bar>( new Bar.Serializer() );
    InputStream in = res.openStream();
    Bar bar = serializer.deserialize( in );

    assertThat( bar.getValue() ).isEqualTo( 7 );
    assertThat( bar.getDescription() ).isEqualTo( "hey" );
  }
}
