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

import org.junit.*;
import org.mozilla.javascript.Context;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 *
 */
public class EmitTest extends AbstractViewTest {
  @Test
  public void testEmit() throws Exception {
    prepare();
    assertEquals( "1--4", Context.toString( evaluate( "emit(1,4);" + EMIT_TO_STRING ) ) );
  }

  @Test
  public void testArrayEmity() throws Exception {
    prepare();
    assertEquals( "[1,2,3]--4", Context.toString( evaluate( "emit([1,2,3],4);" + EMIT_TO_STRING ) ) );
  }

  @Test
  public void testEmptyEmit() throws Exception {
    prepare();
    assertEquals( "undefined", Context.toString( evaluate( AbstractViewTest.EMIT_TO_STRING ) ) );
  }

  private void prepare() throws IOException {
    evaluate( getContent( EMIT_JS ) );
    evaluate( getContent( JSON2 ) );
  }

  @Test
  public void testJson() throws Exception {
    evaluate( getContent( JSON2 ) );
    assertEquals( "{}", evaluate( "JSON.stringify(JSON)" ) );
    assertEquals( "{\"asdf\":123}", Context.toString( evaluate( "JSON.stringify({  'asdf':123})" ) ) );
  }

  @Test
  public void testMultiEmit() throws Exception {
    prepare();
    assertEquals( "0--17\n" +
                    "1--4\n", Context.toString( evaluate( "emit(0,17);emit(1,4);" + EMIT_TO_STRING ) ) );

  }
}
