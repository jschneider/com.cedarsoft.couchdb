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
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * Offers access methods for a couch database
 */
public class CouchDatabase {
  /**
   * The path segment used to access design documents
   */
  @Nonnull
  public static final String PATH_SEGMENT_DESIGN = "_design";
  /**
   * The path segment used to access a view
   */
  @Nonnull
  public static final String PATH_SEGMENT_VIEW = "_view";
  /**
   * Param for revision
   */
  @Nonnull
  public static final String PARAM_REV = "rev";
  /**
   * JSON type
   */
  @Nonnull
  public static final MediaType JSON_TYPE = MediaType.APPLICATION_JSON_TYPE;

  @Nonnull
  private final Client client;
  @Nonnull
  private final ClientFilter[] clientFilters;
  @Nonnull
  private final WebResource dbRoot;
  @Nonnull
  private final ViewResponseSerializer viewResponseSerializer;
  @Nonnull
  private final CouchDocSerializer couchDocSerializer = new CouchDocSerializer();

  /**
   * Creates a new database
   *
   * @param serverUri the server uri
   * @param dbName    the db name (will be appended to the server uri)
   * @param filters   the filters
   */
  public CouchDatabase( @Nonnull URI serverUri, @Nonnull String dbName, @Nullable ClientFilter... filters ) {
    this( serverUri.resolve( "/" + dbName ), filters );
  }

  /**
   * Creates a new database
   *
   * @param uri     the uri
   * @param filters optional filters (e.g. for authentication)
   */
  public CouchDatabase( @Nonnull URI uri, @Nullable ClientFilter... filters ) {
    client = new Client();
    if ( filters != null ) {
      for ( ClientFilter filter : filters ) {
        client.addFilter( filter );
      }
    }
    this.clientFilters = filters == null ? new ClientFilter[0] : filters.clone();
    dbRoot = client.resource( uri );
    viewResponseSerializer = new ViewResponseSerializer( new RowSerializer( couchDocSerializer ) );
  }

  /**
   * Returns the client filters
   *
   * @return the client filters (a cloned instance)
   */
  @Nonnull
  public ClientFilter[] getClientFilters() {
    return clientFilters.clone();
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
  @Nonnull
  public ActionResponse put( @Nonnull DocId id, @Nonnull InputStream content ) throws ActionFailedException {
    WebResource path = dbRoot.path( id.asString() );

    if ( Debug.isEnabled() ) {
      Debug.out().println( "PUT " + path.toString() );
    }

    ClientResponse response = path
      .type( JSON_TYPE ).accept( JSON_TYPE ).put( ClientResponse.class, content );
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
  @Nonnull
  public <T> ActionResponse put( @Nonnull CouchDoc<T> doc, @Nonnull JacksonSerializer<? super T> serializer ) throws ActionFailedException, IOException {
    WebResource path = dbRoot.path( doc.getId().asString() );

    if ( Debug.isEnabled() ) {
      Debug.out().println( "PUT " + path.toString() );
    }

    ClientResponse clientResponse = path.type( JSON_TYPE ).accept( JSON_TYPE ).put( ClientResponse.class, couchDocSerializer.serialize( doc, serializer ) );
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
  @Nonnull
  public ActionResponse put( @Nonnull DocId docId, @Nullable Revision revision, @Nonnull MediaType mediaType, @Nonnull InputStream content ) throws ActionFailedException {
    WebResource resource = dbRoot.path( docId.asString() );

    //Add the revision is necessary
    if ( revision != null ) {
      resource = resource.queryParam( PARAM_REV, revision.asString() );
    }

    if ( Debug.isEnabled() ) {
      Debug.out().println( "HEAD " + resource.toString() );
    }

    WebResource.Builder path = resource.type( mediaType ).accept( JSON_TYPE );

    ClientResponse clientResponse = path.put( ClientResponse.class, content );
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
  @Nonnull
  public ActionResponse put( @Nonnull DocId docId, @Nullable Revision revision, @Nonnull AttachmentId attachmentId, @Nonnull MediaType mediaType, @Nonnull InputStream attachment ) throws ActionFailedException {
    WebResource resource = dbRoot
      .path( docId.asString() )
      .path( attachmentId.asString() );

    //Add the revision is necessary
    if ( revision != null ) {
      resource = resource.queryParam( PARAM_REV, revision.asString() );
    }

    if ( Debug.isEnabled() ) {
      Debug.out().println( "PUT " + resource.toString() );
    }

    ClientResponse clientResponse = resource.type( mediaType ).accept( JSON_TYPE ).put( ClientResponse.class, attachment );
    return ActionResponse.create( clientResponse );
  }

  @Nonnull
  public ActionResponse post( @Nonnull InputStream content ) throws ActionFailedException {
    WebResource.Builder path = dbRoot
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( Debug.isEnabled() ) {
      Debug.out().println( "POST " + path.toString() );
    }

    ClientResponse response = path.post( ClientResponse.class, content );
    return ActionResponse.create( response );
  }

  /**
   * Pushes the object to the database
   *
   * @param object     the object
   * @param serializer the serializer that is used to serialize the object
   * @return the response
   *
   * @noinspection TypeMayBeWeakened
   */
  @Nonnull
  public <T> ActionResponse post( @Nonnull T object, @Nonnull JacksonSerializer<? super T> serializer ) throws ActionFailedException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.serialize( object, out );
    return post( new ByteArrayInputStream( out.toByteArray() ) );
  }

  /**
   * Queries the given view.
   * Each view return
   * <ul>
   *   <li>the key</li>
   *   <li>the value</li>
   *   <li>optionally the document</li>
   * </ul>
   *
   * The serializers assigned as methods parameters are used to deserialize these parts.
   * This method does *not* support included docs.
   *
   *
   * @param viewDescriptor  describes the view
   * @param keySerializer   the serializer used to deserialize the key
   * @param valueSerializer the serializer used to deserialize the value
   * @param options         the options for the query
   * @param <K>             the type of the key
   * @param <V>             the type of the value
   * @return the response
   *
   * @throws InvalidTypeException
   * @throws ActionFailedException
   * @throws IOException
   */
  @Nonnull
  public <K, V> ViewResponse<K, V, Void> query( @Nonnull ViewDescriptor viewDescriptor, @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nullable Options options ) throws InvalidTypeException, ActionFailedException, IOException {
    InputStream stream = query( viewDescriptor, options );
    return viewResponseSerializer.deserialize( keySerializer, valueSerializer, stream );
  }

  @Nonnull
  public <K, V, D> ViewResponse<K, V, D> query( @Nonnull ViewDescriptor viewDescriptor, @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nonnull JacksonSerializer<? extends D> docSerializer, @Nullable Options options ) throws InvalidTypeException, ActionFailedException, IOException {
    Options localOptions = new Options( options ).includeDocs( true );//force include docs

    InputStream stream = query( viewDescriptor, localOptions );
    return viewResponseSerializer.deserialize( keySerializer, valueSerializer, docSerializer, stream );
  }

  @Nonnull
  public InputStream query( @Nonnull ViewDescriptor viewDescriptor, @Nullable Options options ) throws ActionFailedException {
    WebResource viewPath = dbRoot.path( PATH_SEGMENT_DESIGN ).path( viewDescriptor.getDesignDocumentId() ).path( PATH_SEGMENT_VIEW ).path( viewDescriptor.getViewId() );

    if ( options != null ) {
      MultivaluedMap<String, String> params = new MultivaluedMapImpl();
      for ( Map.Entry<String, String> paramEntry : options.getParams().entrySet() ) {
        params.putSingle( paramEntry.getKey(), paramEntry.getValue() );
      }

      viewPath = viewPath.queryParams( params );
    }

    return get( viewPath );
  }

  /**
   * Returns the document
   *
   * @param id the id
   * @return the view as stream
   */
  @Nonnull
  public InputStream get( @Nonnull DocId id ) throws ActionFailedException {
    return get( dbRoot.path( id.asString() ) );
  }

  @Nonnull
  protected InputStream get( @Nonnull WebResource resource ) throws ActionFailedException {
    if ( Debug.isEnabled() ) {
      Debug.out().println( "GET " + resource.toString() );
    }
    ClientResponse response = resource.get( ClientResponse.class );
    ActionResponse.verifyNoError( response );

    if ( Debug.isEnabled() ) {
      try {
        byte[] content = ByteStreams.toByteArray( response.getEntityInputStream() );
        Debug.out().println( new String( content ) );
        return new ByteArrayInputStream( content );
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      }
    }

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
  @Nonnull
  public <T> CouchDoc<T> get( @Nonnull DocId id, @Nonnull JacksonSerializer<T> serializer ) throws ActionFailedException {
    return couchDocSerializer.deserialize( serializer, get( id ) );
  }

  @Nonnull
  public InputStream get( @Nonnull DocId docId, @Nonnull AttachmentId attachmentId ) throws ActionFailedException {
    return get( dbRoot.path( docId.asString() ).path( attachmentId.asString() ) );
  }

  /**
   * Returns the URI
   *
   * @return the URI
   */
  @Nonnull
  public URI getURI() {
    return dbRoot.getURI();
  }

  @Nonnull
  public WebResource getDbRoot() {
    return dbRoot;
  }

  @Nonnull
  protected Client getClient() {
    return client;
  }

  /**
   * This implementation only works for the basic use cases
   *
   * @return the db name
   */
  @Nonnull
  public String getDbName() {
    return getURI().getPath().substring( 1 );
  }

  @Nonnull
  public ActionResponse delete( @Nonnull DocId id, @Nonnull Revision revision ) throws ActionFailedException {
    WebResource.Builder path = dbRoot.path( id.asString() )
      .queryParam( PARAM_REV, revision.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( Debug.isEnabled() ) {
      Debug.out().println( "DELETE " + path.toString() );
    }

    ClientResponse response = path.delete( ClientResponse.class );
    return ActionResponse.create( response );
  }

  @Nonnull
  public ActionResponse delete( @Nonnull DocId id, @Nonnull Revision revision, @Nonnull AttachmentId attachmentId ) throws ActionFailedException {
    WebResource.Builder path = dbRoot
      .path( id.asString() )
      .path( attachmentId.asString() )
      .queryParam( PARAM_REV, revision.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( Debug.isEnabled() ) {
      Debug.out().println( "DELETE " + path.toString() );
    }

    ClientResponse response = path.delete( ClientResponse.class );
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
  @Nonnull
  public Revision getRev( @Nonnull DocId docId ) throws ActionFailedException {
    WebResource.Builder path = dbRoot
      .path( docId.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( Debug.isEnabled() ) {
      Debug.out().println( "HEAD " + path.toString() );
    }

    ClientResponse response = path.head();
    ActionResponse.verifyNoError( response );

    EntityTag entityTag = response.getEntityTag();
    if ( entityTag == null ) {
      throw new IllegalArgumentException( "No Etag found" );
    }
    return new Revision( entityTag.getValue() );
  }

  @Nonnull
  public ClientResponse getHead( @Nonnull DocId docId ) {
    WebResource.Builder path = dbRoot.path( docId.asString() ).type( JSON_TYPE ).accept( JSON_TYPE );

    if ( Debug.isEnabled() ) {
      Debug.out().println( "HEAD " + path.toString() );
    }

    return path.head();
  }

  @Nonnull
  public ClientResponse getHead( @Nonnull DocId docId, @Nonnull AttachmentId attachmentId ) {
    WebResource.Builder path = dbRoot
      .path( docId.asString() )
      .path( attachmentId.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( Debug.isEnabled() ) {
      Debug.out().println( "HEAD " + path.toString() );
    }

    return path.head();
  }

  @Override
  public String toString() {
    return "CouchDatabase{" +
      "dbRoot=" + dbRoot +
      '}';
  }
}
