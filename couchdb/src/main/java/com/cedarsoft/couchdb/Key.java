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

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.impl.JsonWriteContext;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Represents a key that can be used for queries
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class Key {
  @Nonnull
  public static final Key EMPTY_ARRAY = new Key( "[]" );
  @Nonnull
  public static final Key EMPTY_OBJECT = new Key( "{}" );

  @Nonnull
  private final String json;

  /**
   * Creates a new key
   *
   * @param json the json code for this key
   */
  public Key( @Nonnull String json ) {
    this.json = json;
  }

  @Nonnull
  public String getJson() {
    return json;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof Key ) ) {
      return false;
    }

    Key key = ( Key ) o;

    if ( !json.equals( key.json ) ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return json.hashCode();
  }

  @Override
  public String toString() {
    return "Key{" +
      json + '\'' +
      '}';
  }

  /**
   * Creates a new array key
   *
   * @param parts the parts of the array (numbers are interpreted as numbers, every other type is interpreted as string)
   * @return the array key object
   */
  @Nonnull
  public static Key array( @Nonnull Object... parts ) {
    StringBuilder builder = new StringBuilder();
    builder.append( "[" );

    builder.append( createArrayContent( parts ) );

    builder.append( "]" );
    return new Key( builder.toString() );
  }

  /**
   * Returns a key that can be used as "end" array (for "endkey")
   *
   * @param parts the parts
   * @return an array that contains an additional empty object as last element
   */
  @Nonnull
  public static Key endArray( @Nonnull Object... parts ) {
    if ( parts.length == 0 ) {
      throw new IllegalArgumentException( "Need at least one element" );
    }

    StringBuilder builder = new StringBuilder();
    builder.append( "[" );

    builder.append( createArrayContent( parts ) );

    builder.append( ",{}" );
    builder.append( "]" );
    return new Key( builder.toString() );

  }

  @Nonnull
  private static String createArrayContent( @Nonnull Object... parts ) {
    StringBuilder builder = new StringBuilder();

    for ( int i = 0; i < parts.length; i++ ) {
      Object part = parts[i];

      if ( part instanceof Number ) {
        builder.append( part );
      } else if ( part instanceof String ) {
        builder.append( escape( ( String ) part ) );
      } else {
        throw new IllegalArgumentException( "Unknown part at index " + i + ": <" + part + ">" );
      }

      if ( i < parts.length - 1 ) {
        builder.append( "," );
      }
    }

    return builder.toString();
  }

  @Nonnull
  public static Key string( @Nonnull String value ) {
    return new Key( escape( value ) );
  }

  @Nonnull
  public static Key endString( @Nonnull String value ) {
    return new Key( escape( value + "ZZZ" ) );
  }

  @Nonnull
  private static String escape( @Nonnull String value ) {
    try {
      StringWriter out = new StringWriter();
      JsonGenerator generator = new JsonFactory().createJsonGenerator( out );
      generator.writeString( value );
      generator.close();
      return out.toString();
    } catch ( IOException e ) {
      throw new IllegalArgumentException( "Invalid value: <" + value + ">", e );
    }
  }
}
