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

import com.cedarsoft.couchdb.io.CouchDocSerializer;
import com.cedarsoft.couchdb.io.RowSerializer;
import com.cedarsoft.couchdb.io.ViewResponseSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.JacksonSerializer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class CouchDatabase {
  @NonNls
  public static final String PARAM_REV = "rev";

  @NotNull
  private final Client client;
  @NotNull
  private final WebResource dbRoot;
  @NotNull
  private final ViewResponseSerializer viewResponseSerializer;

  @NotNull
  private final CouchDocSerializer couchDocSerializer = new CouchDocSerializer();

  @Deprecated
  public CouchDatabase( @NotNull @NonNls String host, int port, @NotNull @NonNls String dbName ) throws URISyntaxException {
    this( new URI( "http://" + host + ":" + port + "/" + dbName ) );
  }

  public CouchDatabase( @NotNull URI serverUri, @NotNull @NonNls String dbName ) {
    this( serverUri.resolve( "/" + dbName ) );
  }

  public CouchDatabase( @NotNull URI uri ) {
    client = new Client();
    dbRoot = client.resource( uri );
    viewResponseSerializer = new ViewResponseSerializer( new RowSerializer( couchDocSerializer ) );
  }

  /**
   * Puts the document
   *
   * @param id      the document id
   * @param content the content
   * @return the response
   *
   * @throws ActionFailedException
   */
  @NotNull
  public ActionResponse put( @NotNull @NonNls DocId id, @NotNull InputStream content ) throws ActionFailedException {
    ClientResponse response = dbRoot.path( id.asString() ).put( ClientResponse.class, content );
    return ActionResponse.create( response );
  }

  /**
   * Puts the document
   *
   * @param doc        the couch document (contains the object)
   * @param serializer the serializer that is used to serialize the object contained within the document
   * @param <T>        the type
   * @return the response
   *
   * @throws ActionFailedException
   */
  @NotNull
  public <T> ActionResponse put( @NotNull CouchDoc<T> doc, @NotNull JacksonSerializer<? super T> serializer ) throws ActionFailedException {
    ClientResponse clientResponse = dbRoot.path( doc.getId().asString() ).put( ClientResponse.class, couchDocSerializer.serialize( doc, serializer ) );
    ActionResponse actionResponse = ActionResponse.create( clientResponse );

    //Update the rev
    doc.setRev( actionResponse.getRev() );

    return actionResponse;
  }

  /**
   * Puts a document
   *
   * @param docId     the doc id
   * @param revision  the (optional) revision
   * @param mediaType the media type
   * @param content   the content
   * @return the response
   *
   * @throws ActionFailedException
   */
  @NotNull
  public ActionResponse put( @NotNull DocId docId, @Nullable Revision revision, @NotNull MediaType mediaType, @NotNull InputStream content ) throws ActionFailedException {
    WebResource resource = dbRoot
      .path( docId.asString() );

    //Add the revision is necessary
    if ( revision != null ) {
      resource = resource.queryParam( PARAM_REV, revision.asString() );
    }

    ClientResponse clientResponse = resource.type( mediaType ).put( ClientResponse.class, content );
    return ActionResponse.create( clientResponse );
  }

  /**
   * Puts an attachment
   *
   * @param docId        the doc id
   * @param revision     the (optional) revision
   * @param attachmentId the attachment id
   * @param mediaType    the media type
   * @param attachment   the attachment
   * @return the response
   *
   * @throws ActionFailedException
   */
  @NotNull
  public ActionResponse put( @NotNull DocId docId, @Nullable Revision revision, @NotNull AttachmentId attachmentId, @NotNull MediaType mediaType, @NotNull InputStream attachment ) throws ActionFailedException {
    WebResource resource = dbRoot
      .path( docId.asString() )
      .path( attachmentId.asString() );

    //Add the revision is necessary
    if ( revision != null ) {
      resource = resource.queryParam( PARAM_REV, revision.asString() );
    }

    ClientResponse clientResponse = resource.type( mediaType ).put( ClientResponse.class, attachment );
    return ActionResponse.create( clientResponse );
  }

  @Deprecated
  @NotNull
  public <T> ActionResponse putUpdated( @NotNull CouchDoc<T> doc, @NotNull JacksonSerializer<? super T> serializer ) throws ActionFailedException {
    if ( doc.getRev() == null ) {
      throw new IllegalArgumentException( "Cannot update a doc without REV" );
    }

    return put( doc, serializer );
  }

  @NotNull
  @NonNls
  public <K, V> ViewResponse<K, V, Void> query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId, JacksonSerializer<? super K> keySerializer, @NotNull JacksonSerializer<? super V> valueSerializer ) throws InvalidTypeException, ActionFailedException, IOException {
    InputStream stream = query( designDocumentId, viewId );

    return viewResponseSerializer.deserialize( keySerializer, valueSerializer, stream );
  }

  @NotNull
  @NonNls
  public <K, V, D> ViewResponse<K, V, D> query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId, JacksonSerializer<? super K> keySerializer, @NotNull JacksonSerializer<? super V> valueSerializer, @NotNull JacksonSerializer<? extends D> docSerializer ) throws InvalidTypeException, ActionFailedException, IOException {
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
  public InputStream query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId ) throws ActionFailedException {
    return query( designDocumentId, viewId, false );
  }

  @NotNull
  public InputStream query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId, boolean includeDocs ) throws ActionFailedException {
    return query( designDocumentId, viewId, includeDocs, null, null );
  }

  @NotNull
  public InputStream query( @NotNull @NonNls String designDocumentId, @NotNull @NonNls String viewId, boolean includeDocs, @Nullable String startKey, @Nullable String endKey ) throws ActionFailedException {
    WebResource viewPath = dbRoot.path( "_design" ).path( designDocumentId ).path( "_view" ).path( viewId );

    if ( startKey != null ) {
      viewPath = viewPath.queryParam( "startkey", startKey );
    }
    if ( endKey != null ) {
      viewPath = viewPath.queryParam( "endkey", endKey );
    }

    if ( includeDocs ) {
      viewPath = viewPath.queryParam( "include_docs", "true" );
    }

    return get( viewPath );
  }

  /**
   * Returns the document
   *
   * @param id the id
   * @return the view as stream
   */
  @NotNull
  public InputStream get( @NotNull @NonNls DocId id ) throws ActionFailedException {
    return get( dbRoot.path( id.asString() ) );
  }

  @NotNull
  protected InputStream get( @NotNull WebResource resource ) throws ActionFailedException {
    ClientResponse response = resource.get( ClientResponse.class );
    ActionResponse.verifyNoError( response );
    return response.getEntityInputStream();
  }

  /**
   * Returns the document
   *
   * @param id         the id
   * @param serializer the serializer
   * @param <T>        the object type
   * @return the doc
   */
  @NotNull
  public <T> CouchDoc<T> get( @NotNull @NonNls DocId id, @NotNull JacksonSerializer<T> serializer ) throws ActionFailedException {
    return couchDocSerializer.deserialize( serializer, get( id ) );
  }

  @NotNull
  public InputStream get( @NotNull DocId docId, @NotNull AttachmentId attachmentId ) throws ActionFailedException {
    return get( dbRoot.path( docId.asString() ).path( attachmentId.asString() ) );
  }

  /**
   * Returns the URI
   *
   * @return the URI
   */
  @NotNull
  public URI getURI() {
    return dbRoot.getURI();
  }

  @NotNull
  public WebResource getDbRoot() {
    return dbRoot;
  }

  @NotNull
  protected Client getClient() {
    return client;
  }

  /**
   * This implementation only works for the basic use cases
   *
   * @return the db name
   */
  @TestOnly
  @NotNull
  @NonNls
  public String getDbName() {
    return getURI().getPath().substring( 1 );
  }

  @NotNull
  public ActionResponse delete( @NotNull @NonNls DocId id, @NotNull @NonNls Revision revision ) throws ActionFailedException {
    ClientResponse response = dbRoot.path( id.asString() ).queryParam( PARAM_REV, revision.asString() ).delete( ClientResponse.class );
    return ActionResponse.create( response );
  }

  @NotNull
  public ActionResponse delete( @NotNull @NonNls DocId id, @NotNull @NonNls Revision revision, @NotNull AttachmentId attachmentId ) throws ActionFailedException {
    ClientResponse response = dbRoot.path( id.asString() ).path( attachmentId.asString() ).queryParam( PARAM_REV, revision.asString() ).delete( ClientResponse.class );
    return ActionResponse.create( response );
  }

  /**
   * Returns the revision using HEAD
   *
   * @param docId the doc id
   * @return the current revision for the given doc id
   *
   * @throws ActionFailedException
   */
  @NotNull
  public Revision getRev( @NotNull DocId docId ) throws ActionFailedException {
    ClientResponse response = dbRoot.path( docId.asString() ).head();
    ActionResponse.verifyNoError( response );

    EntityTag entityTag = response.getEntityTag();
    if ( entityTag == null ) {
      throw new IllegalArgumentException( "No Etag found" );
    }
    return new Revision( entityTag.getValue() );
  }

  @NotNull
  public ClientResponse getHead( @NotNull DocId docId ) {
    return dbRoot.path( docId.asString() ).head();
  }

  @NotNull
  public ClientResponse getHead( @NotNull DocId docId, @NotNull AttachmentId attachmentId ) {
    return dbRoot.path( docId.asString() ).path( attachmentId.asString() ).head();
  }
}
