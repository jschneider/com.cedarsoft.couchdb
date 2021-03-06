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


import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * A raw couch document - without any further informations
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class RawCouchDoc {
  @Nonnull
  protected final DocId id;
  @Nullable
  protected Revision rev;

  /**
   * Creates a new document
   *
   * @param id the document id
   */
  public RawCouchDoc( @Nonnull DocId id ) {
    this( id, null );
  }

  /**
   * Creates a new document for the given unique id
   *
   * @param uniqueId the unique id (contains a revision)
   */
  public RawCouchDoc( @Nonnull UniqueId uniqueId ) {
    this( uniqueId.getId(), uniqueId.getRevision() );
  }

  /**
   * Creates a new couch document
   *
   * @param id  the id
   * @param rev the revision
   */
  public RawCouchDoc( @Nonnull DocId id, @Nullable Revision rev ) {
    this.id = id;
    this.rev = rev;
  }

  /**
   * Returns the id
   *
   * @return the id
   */
  @Nonnull
  public DocId getId() {
    return id;
  }

  /**
   * Sets the revision. Should only be called when the doc has been updated
   *
   * @param rev the revision
   */
  public void setRev( @Nullable Revision rev ) {
    this.rev = rev;
  }

  /**
   * Returns the revision
   *
   * @return the revision
   */
  @Nullable
  public Revision getRev() {
    return rev;
  }
}
