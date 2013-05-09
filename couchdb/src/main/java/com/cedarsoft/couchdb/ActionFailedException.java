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


import com.google.common.base.Charsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This exception is thrown if an action has failed (http error codes)
 */
public class ActionFailedException extends CouchDbException implements HasRawData {
  @Nonnull
  private final String error;
  @Nonnull
  private final String reason;

  private final int status;

  /**
   * This field *may* contain the raw result - or parts of it.
   * They should only be used for debugging purposes
   */
  @Nullable
  private final byte[] raw;


  public ActionFailedException( int status, @Nonnull String error, @Nonnull String reason, @Nullable byte[] raw ) {
    super( status + " " + error + ": " + reason + formatRaw( raw ) );
    this.status = status;
    this.error = error;
    this.reason = reason;
    //noinspection AssignmentToCollectionOrArrayFieldFromParameter
    this.raw = raw;
  }

  @Nonnull
  private static String formatRaw( @Nullable byte[] raw ) {
    if ( raw == null ) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    builder.append( "\n" );
    builder.append( "\tFirst " ).append( raw.length ).append( " bytes of response:" ).append( "\n" );
    builder.append( "-----------------------------------\n" ).append( new String( raw, Charsets.UTF_8 ) ).append( "\n-----------------------------------" );

    return builder.toString();
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
   * Returns the error message
   *
   * @return the error message
   */
  @Nonnull
  public String getError() {
    return error;
  }

  /**
   * Returns the reason
   *
   * @return the reason
   */
  @Nonnull
  public String getReason() {
    return reason;
  }
}
