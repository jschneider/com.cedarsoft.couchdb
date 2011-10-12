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
package com.cedarsoft.couchdb.io;

import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.couchdb.UniqueId;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.test.compatible.JacksonParserWrapper;
import com.cedarsoft.version.Version;
import com.cedarsoft.version.VersionException;
import com.cedarsoft.version.VersionRange;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * This wrapper skips couchdb specific entries ("_id" and "_rev").
 * <p/>
 * Wraps a default serializer.
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 * @param <T> the type
 */
public class CouchSerializerWrapper<T> extends AbstractJacksonSerializer<T> {
  @Nonnull
  private final AbstractJacksonSerializer<T> delegate;

  public CouchSerializerWrapper( @Nonnull AbstractJacksonSerializer<T> delegate ) {
    super( delegate.getType(), delegate.getFormatVersionRange() );
    if ( !delegate.isObjectType() ) {
      throw new IllegalStateException( "Not supported for object type serializer: " + delegate.getClass().getName() );
    }

    this.delegate = delegate;
  }

  /**
   * Serialization is only supported for test cases. This method just delegates everything
   *
   * @param serializeTo   serialize to
   * @param object        the object
   * @param formatVersion the format version
   * @throws IOException
   * @throws VersionException
   * @throws JsonProcessingException
   */
  @Override
  public void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull T object, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
    delegate.serialize( serializeTo, object, formatVersion );
  }

  @Override
  protected void beforeTypeAndVersion( @Nonnull JacksonParserWrapper wrapper ) throws IOException, JsonProcessingException, InvalidTypeException {
    super.beforeTypeAndVersion( wrapper );

    wrapper.nextFieldValue( "_id" );
    final DocId id = new DocId( wrapper.getText() );
    wrapper.nextFieldValue( "_rev" );
    final Revision revision = new Revision( wrapper.getText() );

    current = new UniqueId( id, revision );
  }

  @Nonnull
  @Override
  public T deserialize( @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
    return delegate.deserialize( deserializeFrom, formatVersion );
  }

  @Nullable
  private UniqueId current;

  /**
   * Returns the current unique id
   *
   * @return the current unique id
   *
   * @noinspection NullableProblems
   */
  @Nonnull
  public UniqueId getCurrent() throws IllegalStateException {
    @Nullable final UniqueId copy = current;
    if ( copy == null ) {
      throw new IllegalStateException( "No current id available" );
    }
    return copy;
  }
}
