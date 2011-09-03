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
import com.cedarsoft.couchdb.io.ActionResponseSerializer;
import com.sun.jersey.api.client.ClientResponse;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * A response for an action like put/delete
 */
public class ActionResponse {
  @Nonnull
  private final UniqueId uniqueId;

  private final int status;

  @Nonnull
  private final URI location;

  public ActionResponse( @Nonnull DocId uniqueId, @Nonnull Revision rev, int status, @Nonnull URI location ) {
    this( new UniqueId( uniqueId, rev ), status, location );
  }

  public ActionResponse( @Nonnull UniqueId uniqueId, int status, @Nonnull URI location ) {
    this.uniqueId = uniqueId;
    this.status = status;
    this.location = location;
  }

  public int getStatus() {
    return status;
  }

  @Nonnull
  public URI getLocation() {
    return location;
  }

  @Nonnull
  public DocId getId() {
    return getUniqueId().getId();
  }

  @Nonnull
  public Revision getRev() {
    return getUniqueId().getRevision();
  }

  @Nonnull
  public UniqueId getUniqueId() {
    return uniqueId;
  }

  @Override
  public String toString() {
    return "ActionResponse{" +
      "uniqueId=" + uniqueId +
      ", status=" + status +
      ", location=" + location +
      '}';
  }

  @Nonnull
  public static ActionResponse create( @Nonnull ClientResponse response ) throws ActionFailedException {
    verifyNoError( response );
    return new ActionResponseSerializer().deserialize( response );
  }

  /**
   * Throws an exception if the response contains a value
   *
   * @param response the response
   * @throws ActionFailedException
   */
  public static void verifyNoError( @Nonnull ClientResponse response ) throws ActionFailedException {
    if ( !isNotSuccessful( response ) ) {
      return;
    }

    if ( !response.hasEntity() ) {
      throw new ActionFailedException( response.getStatus(), "unknown", "unknown" );
    }

    throw new ActionFailedExceptionSerializer().deserialize( response.getStatus(), response.getEntityInputStream() );
  }

  public static boolean isNotSuccessful( @Nonnull ClientResponse response ) {
    return response.getStatus() < 200 || response.getStatus() > 299;
  }
}
