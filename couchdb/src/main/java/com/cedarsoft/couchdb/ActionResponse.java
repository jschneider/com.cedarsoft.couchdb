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

import com.cedarsoft.couchdb.io.ActionFailedExceptionSerializer;
import com.cedarsoft.couchdb.io.CreationResponseSerializer;
import com.sun.jersey.api.client.ClientResponse;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A response for an action like put/delete
 */
public class ActionResponse {
  @NotNull
  @NonNls
  private final DocId id;
  @NotNull
  @NonNls
  private final Revision rev;

  private final int status;

  public ActionResponse( @NotNull DocId id, @NotNull Revision rev, int status ) {
    this.id = id;
    this.rev = rev;
    this.status = status;
  }

  public int getStatus() {
    return status;
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
    return "ActionResponse{" +
      "id=" + id +
      ", rev=" + rev +
      ", status=" + status +
      '}';
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( !( o instanceof ActionResponse ) ) return false;

    ActionResponse success = ( ActionResponse ) o;

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
  public static ActionResponse create( @NotNull ClientResponse response ) throws IOException, ActionFailedException {
    verifyNoError( response );
    return new CreationResponseSerializer().deserialize( response );
  }

  /**
   * Throws an exception if the response contains a value
   *
   * @param response the response
   * @throws ActionFailedException
   * @throws IOException
   */
  public static void verifyNoError( @NotNull ClientResponse response ) throws ActionFailedException, IOException {
    if ( !isNotSuccessful( response ) ) {
      return;
    }

    if ( !response.hasEntity() ) {
      throw new ActionFailedException( "unknown", "unknown" );
    }

    throw new ActionFailedExceptionSerializer().deserialize( response.getEntityInputStream() );
  }

  public static boolean isNotSuccessful( @NotNull ClientResponse response ) {
    return response.getStatus() < 200 || response.getStatus() > 299;
  }
}
