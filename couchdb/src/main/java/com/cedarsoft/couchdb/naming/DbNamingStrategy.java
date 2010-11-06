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

package com.cedarsoft.couchdb.naming;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DbNamingStrategy {
  @NonNls
  public static final String SEPARATOR = "$";
  @NonNls
  public static final String AT_REPLACEMENT = "(at)";
  @NonNls
  public static final String DOT_REPLACEMENT = "_";

  @NotNull
  private static final Pattern AT_PATTERN = Pattern.compile( "@" );
  @NotNull
  private static final Pattern DOT_PATTERN = Pattern.compile( "\\." );

  @NotNull
  @NonNls
  public String getDatabaseName( @NotNull String userId, @NotNull Bucket bucket, @NotNull Type type ) {
    return DOT_PATTERN.matcher(
      AT_PATTERN.matcher( userId + SEPARATOR + bucket.getId() + SEPARATOR + type.getDbExtension() ).replaceAll( AT_REPLACEMENT )
    ).replaceAll( DOT_REPLACEMENT );
  }

  public enum Type {
    DATA( "data" ),
    ATTACHMENTS( "attachments" );

    @NotNull
    @NonNls
    private final String dbExtension;

    Type( @NotNull @NonNls String dbExtension ) {
      this.dbExtension = dbExtension;
    }

    @NotNull
    @NonNls
    public String getDbExtension() {
      return dbExtension;
    }
  }
}
