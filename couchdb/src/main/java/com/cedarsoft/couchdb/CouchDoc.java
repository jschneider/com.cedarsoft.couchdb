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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains version information for objects stored in a CouchDB
 *
 * @param <T> the type of the object object
 */
public class CouchDoc<T> {
  @NotNull
  @NonNls
  private final DocId id;
  @Nullable
  @NonNls
  private Revision rev;

  @NotNull
  private final T object;

  /**
   * Creates a new info object without a revision
   *
   * @param id     the id
   * @param object the object
   */
  public CouchDoc( @NotNull @NonNls DocId id, @NotNull T object ) {
    this( id, ( Revision ) null, object );
  }

  /**
   * Creates a new info object with a revision
   *
   * @param id     the id
   * @param rev    the revision
   * @param object the object
   */
  public CouchDoc( @NotNull @NonNls DocId id, @Nullable @NonNls Revision rev, @NotNull T object ) {
    this.id = id;
    this.rev = rev;
    this.object = object;
  }

  /**
   * Returns the object
   *
   * @return the object
   */
  @NotNull
  public T getObject() {
    return object;
  }

  /**
   * Returns the id
   *
   * @return the id
   */
  @NotNull
  public DocId getId() {
    return id;
  }

  /**
   * Sets the revision. Should only be called when the doc has been updated
   *
   * @param rev the revision
   */
  void setRev( @Nullable Revision rev ) {
    this.rev = rev;
  }

  /**
   * Returns the revision
   *
   * @return the revision
   */
  @Nullable
  @NonNls
  public Revision getRev() {
    return rev;
  }
}