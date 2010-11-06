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

import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.JacksonSerializer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class CouchDatabase {
  @NotNull
  private final Client client;
  @NotNull
  private final WebResource db;
  @NotNull
  private final ViewResponseSerializer viewResponseSerializer;

  @Deprecated
  public CouchDatabase( @NotNull @NonNls String host, int port, @NotNull @NonNls String dbName ) throws URISyntaxException {
    this( new URI( "http://" + host + ":" + port + "/" + dbName ) );
  }

  public CouchDatabase( @NotNull URI serverUri, @NotNull @NonNls String dbName ) {
    this( serverUri.resolve( "/" + dbName ) );
  }

  public CouchDatabase( @NotNull URI uri ) {
    client = new Client();
    db = client.resource( uri );
    viewResponseSerializer = new ViewResponseSerializer( new RowSerializer( new CouchDocSerializer() ) );
  }

  @NotNull
  public <T> CreationResponse createObject( @NotNull CouchDoc<T> info, @NotNull InputStream doc ) throws IOException, CreationFailedException {
    ClientResponse response = db.path( info.getId() ).put( ClientResponse.class, doc );
    return CreationResponse.create( response );
  }

  @NotNull
  @NonNls
  public <K, V> ViewResponse<K, V, Void> query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId, JacksonSerializer<? super K> keySerializer, @NotNull JacksonSerializer<? super V> valueSerializer ) throws IOException, InvalidTypeException {
    InputStream stream = query( designDocumentId, viewId );

    return viewResponseSerializer.deserialize( keySerializer, valueSerializer, stream );
  }

  @NotNull
  @NonNls
  public <K, V, D> ViewResponse<K, V, D> query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId, JacksonSerializer<? super K> keySerializer, @NotNull JacksonSerializer<? super V> valueSerializer, @NotNull JacksonSerializer<? extends D> docSerializer ) throws InvalidTypeException, IOException {
    String type = docSerializer.getType();
    String startKey = "[\"" + type + "\"]";
    String endKey = "[\"" + type + "Z\"]"; //the "Z" is used as high key

    InputStream stream = query( designDocumentId, viewId, true, startKey, endKey );

    return viewResponseSerializer.deserialize( keySerializer, valueSerializer, docSerializer, stream );
  }

  /**
   * Query the given view
   *
   * @param designDocumentId describes the design document
   * @param viewId           describes the view id
   * @return the answer
   */
  @NotNull
  @NonNls
  public InputStream query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId ) {
    return query( designDocumentId, viewId, false );
  }

  @NotNull
  public InputStream query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId, boolean includeDocs ) {
    return query( designDocumentId, viewId, includeDocs, null, null );
  }

  @NotNull
  public InputStream query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId, boolean includeDocs, @Nullable String startKey, @Nullable String endKey ) {
    WebResource viewPath = db.path( "_design" ).path( designDocumentId ).path( "_view" ).path( viewId );

    if ( startKey != null ) {
      viewPath = viewPath.queryParam( "startkey", startKey );
    }
    if ( endKey != null ) {
      viewPath = viewPath.queryParam( "endkey", endKey );
    }

    if ( includeDocs ) {
      viewPath = viewPath.queryParam( "include_docs", "true" );
    }

    return viewPath.get( InputStream.class );
  }

  @NotNull
  public URI getURI() {
    return db.getURI();
  }

  @NotNull
  protected Client getClient() {
    return client;
  }
}
