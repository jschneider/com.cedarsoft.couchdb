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
import com.sun.jersey.client.apache4.ApacheHttpClient4;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Offers access methods for a couch database
 */
public class CouchDatabase {
  @Nonnull
  private static final Logger LOG = Logger.getLogger( CouchDatabase.class.getName() );

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
  @Deprecated
  public CouchDatabase( @Nonnull URI serverUri, @Nonnull String dbName, @Nullable ClientFilter... filters ) {
    this( serverUri.resolve( "/" + dbName ), filters );
  }

  /**
   * Creates a new database
   *
   * @param uri     the uri
   * @param filters optional filters (e.g. for authentication)
   */
  @Deprecated
  public CouchDatabase( @Nonnull URI uri, @Nullable ClientFilter... filters ) {
    this( createClient( filters ), uri );
  }

  /**
   * Creates a new database
   * @param client the client
   * @param uri the uri
   */
  @Deprecated
  public CouchDatabase( @Nonnull Client client, @Nonnull URI uri ) {
    this( client.resource( uri ) );
  }

  @Nonnull
  public static CouchDatabase create( @Nonnull Client client, @Nonnull URI uri ) {
    return new CouchDatabase( client.resource( uri ) );
  }

  @Nonnull
  public static CouchDatabase create( @Nonnull Client client, @Nonnull URI serverUri, @Nonnull String dbName ) {
    return create( client, serverUri.resolve( "/" + dbName ) );
  }

  /**
   * Creates a new database for the given db root
   * @param dbRoot the db root
   */
  public CouchDatabase(@Nonnull WebResource dbRoot) {
    this.dbRoot = dbRoot;
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
  @Nonnull
  public ActionResponse put( @Nonnull DocId id, @Nonnull InputStream content ) throws ActionFailedException {
    WebResource path = dbRoot.path( id.asString() );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "PUT " + path.toString() );
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

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "PUT " + path.toString() );
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

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "HEAD " + resource.toString() );
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

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "PUT " + resource.toString() );
    }

    ClientResponse clientResponse = resource.type( mediaType ).accept( JSON_TYPE ).put( ClientResponse.class, attachment );
    return ActionResponse.create( clientResponse );
  }

  /**
   * Posts the given stream to the db
   *
   * @param content the content
   * @return the action response
   *
   * @throws ActionFailedException
   */
  @Nonnull
  public ActionResponse post( @Nonnull InputStream content ) throws ActionFailedException {
    WebResource.Builder path = dbRoot
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "POST " + path.toString() );
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
   * <li>the key</li>
   * <li>the value</li>
   * <li>optionally the document</li>
   * </ul>
   * <p/>
   * The serializers assigned as methods parameters are used to deserialize these parts.
   * This method does *not* support included docs.
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
    if ( options != null && options.isIncludeDocs() ) {
      throw new IllegalArgumentException( Options.INCLUDE_DOCS + " is not supported without a doc serializer" );
    }

    InputStream stream = query( viewDescriptor, options );
    try {
      return viewResponseSerializer.deserialize( keySerializer, valueSerializer, stream );
    } finally {
      stream.close();
    }
  }

  /**
   * Queries the given view.
   * Each view return
   * <ul>
   * <li>the key</li>
   * <li>the value</li>
   * <li>optionally the document</li>
   * </ul>
   * <p/>
   * The serializers assigned as methods parameters are used to deserialize these parts.
   * This method supports included docs.
   *
   * @param viewDescriptor  describes the view
   * @param keySerializer   the serializer used to deserialize the key
   * @param valueSerializer the serializer used to deserialize the value
   * @param docSerializer   the document serializer
   * @param options         the options for the query - includeDocs(true) is added automatically
   * @param <K>             the type of the key
   * @param <V>             the type of the value
   * @param <D>             the type of the document object
   * @return the response
   *
   * @throws InvalidTypeException
   * @throws ActionFailedException
   * @throws IOException
   */
  @Nonnull
  public <K, V, D> ViewResponse<K, V, D> query( @Nonnull ViewDescriptor viewDescriptor, @Nonnull JacksonSerializer<? super K> keySerializer, @Nonnull JacksonSerializer<? super V> valueSerializer, @Nonnull JacksonSerializer<? extends D> docSerializer, @Nullable Options options ) throws InvalidTypeException, ActionFailedException, IOException {
    Options localOptions;
    if ( options != null && !options.isGroup() ) {
      localOptions = new Options( options ).includeDocs( true ); //force include docs
    } else {
      localOptions = options;
    }

    InputStream stream = query( viewDescriptor, localOptions );
    try {
      return viewResponseSerializer.deserialize( keySerializer, valueSerializer, docSerializer, stream );
    } finally {
      stream.close();
    }
  }

  /**
   * Executes a query to the given view
   *
   * @param viewDescriptor the view descriptor
   * @param options        the (optional) options
   * @return the input stream
   *
   * @throws ActionFailedException
   */
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

  /**
   * Returns the content for the given resource
   *
   * @param resource the resources
   * @return the input stream
   *
   * @throws ActionFailedException
   */
  @Nonnull
  protected InputStream get( @Nonnull WebResource resource ) throws ActionFailedException {
    long start = System.currentTimeMillis();
    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "GET " + resource.toString() );
    }
    ClientResponse response = resource.get( ClientResponse.class );
    long end = System.currentTimeMillis();
    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "Took: " + ( end - start ) + " ms" );
    }

    ActionResponse.verifyNoError( response );

    if ( LOG.isLoggable( Level.FINER ) ) {
      try {
        byte[] content = ByteStreams.toByteArray( response.getEntityInputStream() );
        if ( content.length > 1024 ) {
          LOG.finer( "Showing first 1024 bytes:\n" + new String( content ).substring( 0, 1024 ) + "..." );
        } else {
          LOG.finer( new String( content ) );
        }
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

  /**
   * Returns the content of the given attachment
   *
   * @param docId        the document id
   * @param attachmentId the attachment id
   * @return the content of the attachment
   *
   * @throws ActionFailedException
   */
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

  /**
   * Returns the db root for this database
   *
   * @return the db root for this database
   */
  @Nonnull
  public WebResource getDbRoot() {
    return dbRoot;
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

  /**
   * Deletes a given document
   *
   * @param id       the id
   * @param revision the revision
   * @return the response
   *
   * @throws ActionFailedException
   */
  @Nonnull
  public ActionResponse delete( @Nonnull DocId id, @Nonnull Revision revision ) throws ActionFailedException {
    WebResource.Builder path = dbRoot.path( id.asString() )
      .queryParam( PARAM_REV, revision.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "DELETE " + path.toString() );
    }

    ClientResponse response = path.delete( ClientResponse.class );
    return ActionResponse.create( response );
  }

  /**
   * Deletes the given attachment
   *
   * @param id           the id
   * @param revision     the revision
   * @param attachmentId the attachment id
   * @return the response
   *
   * @throws ActionFailedException
   */
  @Nonnull
  public ActionResponse delete( @Nonnull DocId id, @Nonnull Revision revision, @Nonnull AttachmentId attachmentId ) throws ActionFailedException {
    WebResource.Builder path = dbRoot
      .path( id.asString() )
      .path( attachmentId.asString() )
      .queryParam( PARAM_REV, revision.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "DELETE " + path.toString() );
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

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "HEAD " + path.toString() );
    }

    ClientResponse response = path.head();
    try {
      ActionResponse.verifyNoError( response );

      EntityTag entityTag = response.getEntityTag();
      if ( entityTag == null ) {
        throw new IllegalArgumentException( "No Etag found" );
      }
      return new Revision( entityTag.getValue() );
    } finally {
      response.close();
    }
  }

  /**
   * Returns the head for the given doc
   *
   * @param docId the doc
   * @return the head for the given doc id
   */
  @Nonnull
  public ClientResponse getHead( @Nonnull DocId docId ) {
    WebResource.Builder path = dbRoot.path( docId.asString() ).type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "HEAD " + path.toString() );
    }

    return path.head();
  }

  /**
   * Returns the head for the given attachment
   *
   * @param docId        the doc id
   * @param attachmentId the attachment id
   * @return the  response
   */
  @Nonnull
  public ClientResponse getHead( @Nonnull DocId docId, @Nonnull AttachmentId attachmentId ) {
    WebResource.Builder path = dbRoot
      .path( docId.asString() )
      .path( attachmentId.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "HEAD " + path.toString() );
    }

    return path.head();
  }

  @Override
  public String toString() {
    return "CouchDatabase{" +
      "dbRoot=" + dbRoot +
      '}';
  }


  @Nonnull
  private static ApacheHttpClient4 createClient( @Nullable ClientFilter[] filters ) {
    ApacheHttpClient4 client = ApacheHttpClient4.create();
    if ( filters == null ) {
      return client;
    }

    for ( ClientFilter filter : filters ) {
      client.addFilter( filter );
    }
    return client;
  }
}
