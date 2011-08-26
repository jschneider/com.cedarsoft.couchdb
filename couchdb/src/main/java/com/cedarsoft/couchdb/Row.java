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


import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * @param <K> the type of the key
 * @param <V> the type of the value
 * @param <D> the type of the document (if there is one)
 * @noinspection ClassNamingConvention
 */
public class Row<K, V, D> {
  @Nonnull
  private final DocId id;
  @Nonnull
  private final K key;
  @Nullable
  private final V value;

  @Nullable
  private final CouchDoc<? extends D> doc;

  public Row( @Nonnull DocId id, @Nonnull K key, @Nullable V value ) {
    this( id, key, value, null );
  }

  public Row( @Nonnull DocId id, @Nonnull K key, @Nullable V value, @Nullable CouchDoc<? extends D> doc ) {
    this.id = id;
    this.key = key;
    this.value = value;
    this.doc = doc;
  }

  @Nonnull
  public DocId getId() {
    return id;
  }

  @Nonnull
  public K getKey() {
    return key;
  }

  @Nullable
  public V getValue() {
    return value;
  }

  @Nullable
  public CouchDoc<? extends D> getDoc() {
    return doc;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof Row ) ) {
      return false;
    }

    Row<?,?,?> row = ( Row<?,?,?> ) o;

    if ( !id.equals( row.id ) ) {
      return false;
    }
    if ( !key.equals( row.key ) ) {
      return false;
    }
    if ( doc != null ? !doc.equals( row.doc ) : row.doc != null ) {
      return false;
    }
    if ( value != null ? !value.equals( row.value ) : row.value != null ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + key.hashCode();
    result = 31 * result + ( value != null ? value.hashCode() : 0 );
    result = 31 * result + ( doc != null ? doc.hashCode() : 0 );
    return result;
  }
}
