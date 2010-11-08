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

import com.cedarsoft.couchdb.io.DeletionFailedExceptionSerializer;
import com.cedarsoft.couchdb.io.DeletionResponseSerializer;
import com.sun.jersey.api.client.ClientResponse;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 */
public class DeletionResponse {
  @NotNull
  @NonNls
  private final DocId id;
  @NotNull
  @NonNls
  private final Revision rev;

  public DeletionResponse( @NotNull DocId id, @NotNull Revision rev ) {
    this.id = id;
    this.rev = rev;
  }

  @NotNull
  @NonNls
  public DocId getId() {
    return id;
  }

  @NotNull
  @NonNls
  public Revision getRev() {
    return rev;
  }

  @Override
  public String toString() {
    return "Success{" +
      "id='" + id + '\'' +
      ", rev='" + rev + '\'' +
      '}';
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( !( o instanceof DeletionResponse ) ) return false;

    DeletionResponse success = ( DeletionResponse ) o;

    if ( !id.equals( success.id ) ) return false;
    if ( !rev.equals( success.rev ) ) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + rev.hashCode();
    return result;
  }

  @NotNull
  public static DeletionResponse create( @NotNull ClientResponse response ) throws IOException, DeletionFailedException {
    if ( response.getStatus() != 200 ) {
      throw new DeletionFailedExceptionSerializer().deserialize( response.getEntityInputStream() );
    }

    return new DeletionResponseSerializer().deserialize( response.getEntityInputStream() );
  }
}
