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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A view response
 *
 * @param <TRowKey> the type of the key (for the rows)
 * @param <TRowValue> the type of the value (for the rows)
 * @param <TDocument> the type of the document (for the rows)
 */
public class ViewResponse<TRowKey, TRowValue, TDocument> {
  private final int totalRows;
  private final int offset;

  @Nonnull
  private final List<Row<TRowKey, TRowValue, TDocument>> rows;

  /**
   * Creates a new view response
   * @param totalRows the amount of total rows
   * @param offset the offset
   * @param rows the rows
   */
  public ViewResponse( int totalRows, int offset, @Nonnull List<Row<TRowKey, TRowValue, TDocument>> rows ) {
    this.totalRows = totalRows;
    this.offset = offset;
    this.rows = new ArrayList<>( rows );
  }

  public int getTotalRows() {
    return totalRows;
  }

  public int getOffset() {
    return offset;
  }

  @Nonnull
  public List<? extends Row<TRowKey, TRowValue, TDocument>> getRows() {
    return Collections.unmodifiableList( rows );
  }

  @Nonnull
  public List<? extends TDocument> getRowObjects() {
    List<TDocument> objects = new ArrayList<>();

    for ( Row<?, ?, TDocument> row : getRows() ) {
      CouchDoc<? extends TDocument> doc = row.getDoc();

      if ( doc == null ) {
        objects.add( null );
      } else {
        objects.add( doc.getObject() );
      }
    }

    return objects;
  }

  /**
   * Returns the value objects
   * @return the value objects
   */
  @Nonnull
  public List<? extends TRowValue> getValueObjects() {
    List<TRowValue> objects = new ArrayList<>();

    for ( Row<?, TRowValue, ?> row : getRows() ) {
      @Nullable TRowValue value = row.getValue();
      objects.add( value );
    }

    return objects;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof ViewResponse ) ) {
      return false;
    }

    ViewResponse<?,?,?> that = ( ViewResponse<?,?,?> ) obj;

    if ( offset != that.offset ) {
      return false;
    }
    if ( totalRows != that.totalRows ) {
      return false;
    }
    if ( !rows.equals( that.rows ) ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = totalRows;
    result = 31 * result + offset;
    result = 31 * result + rows.hashCode();
    return result;
  }
}
