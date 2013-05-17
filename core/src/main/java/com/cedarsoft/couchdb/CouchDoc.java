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

import com.google.common.io.ByteStreams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Contains version information for objects stored in a CouchDB
 *
 * @param <T> the type of the object object
 */
public class CouchDoc<T> extends RawCouchDoc {
  @Nonnull
  private final T object;

  @Nonnull
  private final List<Attachment> attachments = new ArrayList<>();

  /**
   * Creates a new info object without a revision
   *
   * @param id     the id
   * @param object the object
   */
  public CouchDoc( @Nonnull DocId id, @Nonnull T object ) {
    this( id, null, object );
  }

  public CouchDoc( @Nonnull UniqueId uniqueId, @Nonnull T object ) {
    this( uniqueId.getId(), uniqueId.getRevision(), object );
  }

  /**
   * Creates a new info object with a revision
   *
   * @param id     the id
   * @param rev    the revision
   * @param object the object
   */
  public CouchDoc( @Nonnull DocId id, @Nullable Revision rev, @Nonnull T object ) {
    super( id, rev );
    this.object = object;
  }

  /**
   * Returns the object
   *
   * @return the object
   */
  @Nonnull
  public T getObject() {
    return object;
  }

  public void addAttachment( @Nonnull Attachment attachment ) {
    this.attachments.add( attachment );
  }

  @Nonnull
  public List<? extends Attachment> getAttachments() {
    return Collections.unmodifiableList( attachments );
  }

  public boolean hasAttachments() {
    return !attachments.isEmpty();
  }

  /**
   * Returns whether the given document contains any inline attachments
   * @return true if the doc has inline attachments, false otherwise
   */
  public boolean hasInlineAttachments() {
    for ( Attachment attachment : attachments ) {
      if ( attachment.isInline() ) {
        return true;
      }
    }
    return false;
  }

  public void addAttachments( @Nonnull Collection<? extends Attachment> additionalAttachments ) {
    this.attachments.addAll( additionalAttachments );
  }

  /**
   * Do not instantiate this class directly. Instead use one of its sub classes
   */
  public abstract static class Attachment {
    @Nonnull
    private final MediaType contentType;

    @Nonnull
    private final AttachmentId id;

    private Attachment( @Nonnull AttachmentId id, @Nonnull MediaType contentType ) {
      this.id = id;
      this.contentType = contentType;
    }

    @Nonnull
    public AttachmentId getId() {
      return id;
    }

    @Nonnull
    public MediaType getContentType() {
      return contentType;
    }

    public boolean isInline() {
      return false;
    }

    public abstract long getLength();

    @Nonnull
    public abstract byte[] getData();
  }

  public static class StubbedAttachment extends Attachment {
    private final long length;

    public StubbedAttachment( @Nonnull AttachmentId id, @Nonnull MediaType contentType, long length ) {
      super( id, contentType );
      this.length = length;
    }

    @Override
    public long getLength() {
      return length;
    }

    @Nonnull
    @Override
    public byte[] getData() {
      throw new UnsupportedOperationException( "Cannot get data for stub attachment" );
    }
  }

  public static class InlineAttachment extends Attachment {
    @Nonnull
    private final byte[] data;

    public InlineAttachment( @Nonnull AttachmentId id, @Nonnull MediaType mediaType, @Nonnull InputStream content ) throws IOException {
      this( id, mediaType, ByteStreams.toByteArray( content ) );
    }

    public InlineAttachment( @Nonnull AttachmentId id, @Nonnull MediaType contentType, @Nonnull byte[] data ) {
      super( id, contentType );
      this.data = data.clone();
    }

    @Override
    @Nonnull
    public byte[] getData() {
      return data.clone();
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
