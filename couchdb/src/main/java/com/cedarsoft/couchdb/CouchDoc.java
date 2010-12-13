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

import com.sun.jersey.core.util.Base64;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains version information for objects stored in a CouchDB
 *
 * @param <T> the type of the object object
 */
public class CouchDoc<T> extends RawCouchDoc {
  @NotNull
  private final T object;

  @NotNull
  private final List<Attachment> attachments = new ArrayList<Attachment>();

  /**
   * Creates a new info object without a revision
   *
   * @param id     the id
   * @param object the object
   */
  public CouchDoc( @NotNull @NonNls DocId id, @NotNull T object ) {
    this( id, null, object );
  }

  /**
   * Creates a new info object with a revision
   *
   * @param id     the id
   * @param rev    the revision
   * @param object the object
   */
  public CouchDoc( @NotNull @NonNls DocId id, @Nullable @NonNls Revision rev, @NotNull T object ) {
    super( id, rev );
    this.object = object;
  }

  /**
   * Returns the object
   *
   * @return the object
   */
  @NotNull
  public T getObject() {
    return object;
  }

  public void addAttachment( @NotNull Attachment attachment ) {
    this.attachments.add( attachment );
  }

  @NotNull
  public List<? extends Attachment> getAttachments() {
    return Collections.unmodifiableList( attachments );
  }

  public boolean hasAttachments() {
    return !attachments.isEmpty();
  }

  public boolean hasInlineAttachments() {
    for ( Attachment attachment : attachments ) {
      if ( attachment.isInline() ) {
        return true;
      }
    }
    return false;
  }

  public void addAttachments( @NotNull List<? extends Attachment> attachments ) {
    this.attachments.addAll( attachments );
  }

  /**
   * Do not instantiate this class directly. Instead use one of its sub classes
   */
  public abstract static class Attachment {
    @NotNull
    private final MediaType contentType;

    @NotNull
    @NonNls
    private final String id;

    private Attachment( @NonNls @NotNull String id, @NotNull MediaType contentType ) {
      this.id = id;
      this.contentType = contentType;
    }

    @NotNull
    @NonNls
    public String getId() {
      return id;
    }

    @NotNull
    public MediaType getContentType() {
      return contentType;
    }

    public boolean isInline() {
      return false;
    }

    public abstract long getLength();

    @NotNull
    public abstract byte[] getData();
  }

  public static class StubbedAttachment extends Attachment {
    private final long length;

    public StubbedAttachment( @NonNls @NotNull String id, @NotNull MediaType contentType, long length ) {
      super( id, contentType );
      this.length = length;
    }

    public long getLength() {
      return length;
    }

    @NotNull
    @Override
    public byte[] getData() {
      throw new UnsupportedOperationException( "Cannot get data for stub attachment" );
    }
  }

  public static class InlineAttachment extends Attachment {
    @NotNull
    @NonNls
    private final byte[] data;

    public InlineAttachment( @NonNls @NotNull String id, @NotNull MediaType contentType, @NotNull @NonNls byte[] data ) {
      super( id, contentType );
      this.data = data.clone();
    }

    @NotNull
    @NonNls
    public byte[] getData() {
      return data.clone();
    }

    @NotNull
    @NonNls
    public byte[] getDataEncoded() {
      return data;
    }

    @Override
    public boolean isInline() {
      return true;
    }

    @Override
    public long getLength() {
      throw new UnsupportedOperationException( "no length is available" );
    }
  }
}
