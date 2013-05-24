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

package com.cedarsoft.couchdb.test;

import com.cedarsoft.serialization.jackson.JacksonParserWrapper;
import com.cedarsoft.version.Version;
import com.cedarsoft.version.VersionException;
import com.cedarsoft.version.VersionRange;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 *
 */
public class Foo {
  private final int aValue;
  private final String description;

  public Foo( int aValue, String description ) {
    this.aValue = aValue;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public int getaValue() {
    return aValue;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( !( o instanceof Foo ) ) return false;

    Foo foo = ( Foo ) o;

    if ( aValue != foo.aValue ) return false;
    if ( description != null ? !description.equals( foo.description ) : foo.description != null ) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = aValue;
    result = 31 * result + ( description != null ? description.hashCode() : 0 );
    return result;
  }

  public static class Serializer extends AbstractJacksonSerializer<Foo> {
    public Serializer() {
      super( "foo", VersionRange.single( 1, 0, 0 ) );
    }

    @Override
    public void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull Foo object, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
      serializeTo.writeNumberField( "aValue", object.getaValue() );
      serializeTo.writeStringField( "description", object.getDescription() );
    }

    @Nonnull
    @Override
    public Foo deserialize( @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
      JacksonParserWrapper parserWrapper = new JacksonParserWrapper( deserializeFrom );
      parserWrapper.nextFieldValue( "aValue" );
      int value = deserializeFrom.getIntValue();

      parserWrapper.nextFieldValue( "description" );
      String description = deserializeFrom.getText();

      //Close it
      deserializeFrom.nextToken();
      return new Foo( value, description );
    }
  }
}
