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

import com.cedarsoft.Version;
import com.cedarsoft.VersionException;
import com.cedarsoft.VersionRange;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class Bar {
  private int value;
  private String description;

  public Bar( int value, String description ) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public void setValue( int value ) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }
  
  public static class Serializer extends AbstractJacksonSerializer<Bar> {
    public Serializer() {
      super( "bar", VersionRange.single( 1, 0, 0 ) );
    }

    @Override
    public void serialize( @NotNull JsonGenerator serializeTo, @NotNull Bar object, @NotNull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
      serializeTo.writeNumberField( "value", object.getValue() );
      serializeTo.writeStringField( "description", object.getDescription() );
    }

    @NotNull
    @Override
    public Bar deserialize( @NotNull JsonParser deserializeFrom, @NotNull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
      nextFieldValue( deserializeFrom, "value" );
      int value = deserializeFrom.getIntValue();

      nextFieldValue( deserializeFrom, "description" );
      String description = deserializeFrom.getText();

      closeObject( deserializeFrom );
      return new Bar( value, description );
    }
  }
  
}