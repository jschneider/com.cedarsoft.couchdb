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

import com.sun.jersey.api.client.ClientResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import java.net.URI;

/**
 * A response for an action like put/delete
 */
public class ActionResponse implements HasRawData {
  /**
   * Returns true if the response has not been successful (does not contain a status code of 200-299)
   *
   * @param response the response
   * @return true if the response has not been successful
   */
  public static boolean isNotSuccessful( @WillNotClose @Nonnull ClientResponse response ) {
    return !isSuccessful( response );
  }

  public static boolean isSuccessful( @WillNotClose @Nonnull ClientResponse response ) {
    //noinspection MagicNumber
    return response.getStatus() >= 200 && response.getStatus() <= 299;
  }

  public static final int MAX_RAW_LENGTH = 8192;

  @Nonnull
  private final UniqueId uniqueId;

  private final int status;

  @Nonnull
  private final URI location;

  /**
   * This field *may* contain the raw result - or parts of it.
   * They should only be used for debugging purposes
   */
  @Nullable
  private final byte[] raw;

  /**
   * Creates a new action response
   *
   * @param docId    the id
   * @param rev      the revision
   * @param status   the status code
   * @param location the location
   */
  public ActionResponse( @Nonnull DocId docId, @Nonnull Revision rev, int status, @Nonnull URI location ) {
    this( docId, rev, status, location, null );
  }

  public ActionResponse( @Nonnull DocId docId, @Nonnull Revision rev, int status, @Nonnull URI location, @Nullable byte[] raw ) {
    this( new UniqueId( docId, rev ), status, location, raw );
  }

  /**
   * Creates a new response
   *
   * @param uniqueId the unique id (doc id and revision)
   * @param status   the status code
   * @param location the location
   */
  public ActionResponse( @Nonnull UniqueId uniqueId, int status, @Nonnull URI location ) {
    this( uniqueId, status, location, null );
  }

  public ActionResponse( @Nonnull UniqueId uniqueId, int status, @Nonnull URI location, @Nullable byte[] raw ) {
    this.uniqueId = uniqueId;
    this.status = status;
    this.location = location;
    //noinspection AssignmentToCollectionOrArrayFieldFromParameter
    this.raw = raw;
    if ( raw != null && raw.length > MAX_RAW_LENGTH ) {
      throw new IllegalArgumentException( "raw bytes are too long: was <" + raw.length + "> but max length is <" + MAX_RAW_LENGTH + "" );
    }
  }

  @Override
  @Nullable
  public byte[] getRaw() {
    //noinspection ReturnOfCollectionOrArrayField
    return raw;
  }

  /**
   * Returns the html status code
   *
   * @return the html status code
   */
  public int getStatus() {
    return status;
  }

  /**
   * The location
   *
   * @return the location
   */
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
}
