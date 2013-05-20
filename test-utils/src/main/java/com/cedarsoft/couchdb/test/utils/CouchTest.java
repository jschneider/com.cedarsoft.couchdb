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

import com.cedarsoft.couchdb.CouchDatabase;
import com.cedarsoft.couchdb.core.CouchDbException;
import com.cedarsoft.couchdb.CouchServer;
import com.cedarsoft.couchdb.update.DesignDocumentsProvider;
import org.junit.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public abstract class CouchTest {
  @Rule
  public CouchDbRule couchDbRule = new CouchDbRule( getDesignDocumentsProvider() );

  @Nonnull
  protected CouchDatabase db() {
    return couchDbRule.getCurrentDb();
  }

  @Nonnull
  public CouchDatabase createDb( @Nonnull String name ) throws IOException, URISyntaxException, CouchDbException {
    return couchDbRule.createDb( name );
  }

  @Nonnull
  public CouchDbRule getCouchDbRule() {
    return couchDbRule;
  }

  @Nonnull
  protected CouchServer server() {
    return couchDbRule.getCurrentServer();
  }

  /**
   * Returns the provider for the design documents
   *
   * @return the provider for the design documents
   */
  @Nullable
  protected DesignDocumentsProvider getDesignDocumentsProvider() {
    return null;
  }
}
