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

import com.cedarsoft.CanceledException;
import com.cedarsoft.couchdb.test.CouchDbRule;
import org.jcouchdb.db.Server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public abstract class CouchTest {
  @Rule
  public CouchDbRule couchDbRule = new CouchDbRule( getViewResource() );

  @Nonnull
  protected CouchDatabase db() {
    return couchDbRule.getCurrentDb();
  }

  @Nonnull
  public CouchDatabase createDb( @Nonnull  String name ) throws IOException, URISyntaxException, CouchDbException {
    return couchDbRule.createDb( name );
  }

  @Nonnull
  public CouchDbRule getCouchDbRule() {
    return couchDbRule;
  }

  @Nonnull
  protected Server server() {
    return couchDbRule.getCurrentServer();
  }

  /**
   * Returns one view resource that is used to find the base dir for all views
   *
   * @return one view resource
   *
   * @throws CanceledException if no views shall be uploaded for the test
   */
  @Nullable
  protected URL getViewResource() {
    return null;
  }
}
