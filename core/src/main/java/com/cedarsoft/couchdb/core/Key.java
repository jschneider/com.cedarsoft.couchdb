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
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof Key ) ) {
      return false;
    }

    Key key = ( Key ) obj;

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
    return quote( value );
  }


  /*
  Copied from
  http://svn.codehaus.org/jettison/trunk/src/main/java/org/codehaus/jettison/json/JSONObject.java

  Copyright (c) 2002 JSON.org

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */

  /**
   * Produce a string in double quotes with backslash sequences in all the
   * right places. A backslash will be inserted within </, allowing JSON
   * text to be delivered in HTML. In JSON text, a string cannot contain a
   * control character or an unescaped quote or backslash.
   *
   * @param string A String
   * @return A String correctly formatted for insertion in a JSON text.
   */
  public static String quote( String string ) {
    if ( string == null || string.length() == 0 ) {
      return "\"\"";
    }

    char c = 0;
    int i;
    int len = string.length();
    StringBuilder sb = new StringBuilder( len + 4 );
    String t;

    sb.append( '"' );
    for ( i = 0; i < len; i += 1 ) {
      c = string.charAt( i );
      switch ( c ) {
        case '\\':
        case '"':
          sb.append( '\\' );
          sb.append( c );
          break;
        case '/':
          //                if (b == '<') {
          sb.append( '\\' );
          //                }
          sb.append( c );
          break;
        case '\b':
          sb.append( "\\b" );
          break;
        case '\t':
          sb.append( "\\t" );
          break;
        case '\n':
          sb.append( "\\n" );
          break;
        case '\f':
          sb.append( "\\f" );
          break;
        case '\r':
          sb.append( "\\r" );
          break;
        default:
          if ( c < ' ' ) {
            t = "000" + Integer.toHexString( c );
            sb.append( "\\u" + t.substring( t.length() - 4 ) );
          } else {
            sb.append( c );
          }
      }
    }
    sb.append( '"' );
    return sb.toString();
  }
}
