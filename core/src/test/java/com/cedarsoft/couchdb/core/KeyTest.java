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
package com.cedarsoft.couchdb.core;

import junit.framework.TestCase;
import org.assertj.core.api.Assertions;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class KeyTest extends TestCase {
  public void testDefault() throws Exception {
    assertThat( new Key( "asdf" ) ).isEqualTo( new Key( "asdf" ) );
  }

  public void testArray() throws Exception {
    assertThat( Key.array().getJson() ).isEqualTo( "[]" );
    assertThat( Key.array( "a", "b", "c" ).getJson() ).isEqualTo( "[\"a\",\"b\",\"c\"]" );
    assertThat( Key.array( "a", "b\"", "c" ).getJson() ).isEqualTo( "[\"a\",\"b\\\"\",\"c\"]" );
  }

  public void testEndKey() throws Exception {
    try {
      Key.endArray();
      fail( "Where is the Exception" );
    } catch ( IllegalArgumentException e ) {
      Assertions.assertThat(e ).hasMessage("Need at least one element" );
    }

    assertThat( Key.endArray( "a" ).getJson() ).isEqualTo( "[\"a\",{}]" );
  }

  public void testIntegers() throws Exception {
    assertThat( Key.array( "a", 1, "c" ).getJson() ).isEqualTo( "[\"a\",1,\"c\"]" );
  }

  public void testString() throws Exception {
    assertThat( Key.string( "asdf" ).getJson() ).isEqualTo( "\"asdf\"" );
    assertThat( Key.string( "as\"df" ).getJson() ).isEqualTo( "\"as\\\"df\"" );
  }
}
