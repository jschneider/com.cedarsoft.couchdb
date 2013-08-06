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

import com.cedarsoft.couchdb.core.AbstractCouchDatabase;
import com.cedarsoft.couchdb.core.ActionFailedException;
import com.cedarsoft.couchdb.core.ActionResponse;
import com.cedarsoft.couchdb.core.AttachmentId;
import com.cedarsoft.couchdb.core.CouchDoc;
import com.cedarsoft.couchdb.core.DocId;
import com.cedarsoft.couchdb.core.Options;
import com.cedarsoft.couchdb.core.Revision;
import com.cedarsoft.couchdb.core.ViewDescriptor;
import com.cedarsoft.couchdb.core.ViewResponse;
import com.cedarsoft.couchdb.io.ActionResponseSerializer;
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
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

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

/**
 * Offers access methods for a couch database
 */
public class CouchDatabase extends AbstractCouchDatabase {
  @Nonnull
  public static CouchDatabase create( @Nonnull URI uri, @Nullable ClientFilter... filters ) {
    ClientConnectionManager connectionManager = new ThreadSafeClientConnManager();
    DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
    config.getProperties().put( ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager );

    Client client = ApacheHttpClient4.create( config );

    if ( filters != null ) {
      for ( ClientFilter filter : filters ) {
        client.addFilter( filter );
      }
    }
    return create( client, uri );
  }

  @Nonnull
  public static CouchDatabase create( @Nonnull Client client, @Nonnull URI uri ) {
    return new CouchDatabase( client.resource( uri ) );
  }

  @Nonnull
  public static CouchDatabase create( @Nonnull Client client, @Nonnull URI serverUri, @Nonnull String dbName ) {
    return create( client, serverUri.resolve( "/" + dbName ) );
  }

  @Nonnull
  private final ViewResponseSerializer viewResponseSerializer;
  @Nonnull
  private final CouchDocSerializer couchDocSerializer = new CouchDocSerializer();

  /**
   * Creates a new database for the given db root
   * @param dbRoot the db root
   */
  public CouchDatabase(@Nonnull WebResource dbRoot) {
    super( dbRoot );
    viewResponseSerializer = new ViewResponseSerializer( new RowSerializer( couchDocSerializer ) );
  }

  @Override
  @Nonnull
  public ActionResponse put( @Nonnull DocId id, @Nonnull InputStream content ) throws ActionFailedException {
    WebResource path = getDbRoot().path( id.asString() );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "PUT " + path.toString() );
    }

    ClientResponse response = path
      .type( JSON_TYPE ).accept( JSON_TYPE ).put( ClientResponse.class, content );
    return ActionResponseSerializer.create( response );
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
    WebResource path = getDbRoot().path( doc.getId().asString() );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "PUT " + path.toString() );
    }

    ClientResponse clientResponse = path.type( JSON_TYPE ).accept( JSON_TYPE ).put( ClientResponse.class, couchDocSerializer.serialize( doc, serializer ) );
    ActionResponse actionResponse = ActionResponseSerializer.create( clientResponse );

    //Update the rev
    doc.setRev( actionResponse.getRev() );

    return actionResponse;
  }

  @Override
  @Nonnull
  public ActionResponse put( @Nonnull DocId docId, @Nullable Revision revision, @Nonnull MediaType mediaType, @Nonnull InputStream content ) throws ActionFailedException {
    WebResource resource = getDbRoot().path( docId.asString() );

    //Add the revision is necessary
    if ( revision != null ) {
      resource = resource.queryParam( PARAM_REV, revision.asString() );
    }

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "HEAD " + resource.toString() );
    }

    WebResource.Builder path = resource.type( mediaType ).accept( JSON_TYPE );

    ClientResponse clientResponse = path.put( ClientResponse.class, content );
    return ActionResponseSerializer.create( clientResponse );
  }

  @Override
  @Nonnull
  public ActionResponse put( @Nonnull DocId docId, @Nullable Revision revision, @Nonnull AttachmentId attachmentId, @Nonnull MediaType mediaType, @Nonnull InputStream attachment ) throws ActionFailedException {
    WebResource resource = getDbRoot()
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
    return ActionResponseSerializer.create( clientResponse );
  }

  @Override
  @Nonnull
  public ActionResponse post( @Nonnull InputStream content ) throws ActionFailedException {
    WebResource.Builder path = getDbRoot()
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "POST " + path.toString() );
    }

    ClientResponse response = path.post( ClientResponse.class, content );
    return ActionResponseSerializer.create( response );
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

    try ( InputStream stream = query( viewDescriptor, options ) ) {
      return viewResponseSerializer.deserialize( keySerializer, valueSerializer, stream );
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

    try ( InputStream stream = query( viewDescriptor, localOptions ) ) {
      return viewResponseSerializer.deserialize( keySerializer, valueSerializer, docSerializer, stream );
    }
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

  @Override
  @Nonnull
  public ActionResponse delete( @Nonnull DocId id, @Nonnull Revision revision ) throws ActionFailedException {
    WebResource.Builder path = getDbRoot().path( id.asString() )
      .queryParam( PARAM_REV, revision.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "DELETE " + path.toString() );
    }

    ClientResponse response = path.delete( ClientResponse.class );
    return ActionResponseSerializer.create( response );
  }

  @Override
  @Nonnull
  public ActionResponse delete( @Nonnull DocId id, @Nonnull Revision revision, @Nonnull AttachmentId attachmentId ) throws ActionFailedException {
    WebResource.Builder path = getDbRoot()
      .path( id.asString() )
      .path( attachmentId.asString() )
      .queryParam( PARAM_REV, revision.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "DELETE " + path.toString() );
    }

    ClientResponse response = path.delete( ClientResponse.class );
    return ActionResponseSerializer.create( response );
  }

  @Override
  @Nonnull
  public Revision getRev( @Nonnull DocId docId ) throws ActionFailedException {
    WebResource.Builder path = getDbRoot()
      .path( docId.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "HEAD " + path.toString() );
    }

    ClientResponse response = path.head();
    try {
      ActionResponseSerializer.verifyNoError( response );

      EntityTag entityTag = response.getEntityTag();
      if ( entityTag == null ) {
        throw new IllegalArgumentException( "No Etag found" );
      }
      return new Revision( entityTag.getValue() );
    } finally {
      response.close();
    }
  }

  @Override
  @Nonnull
  public InputStream get( @Nonnull WebResource resource ) throws ActionFailedException {
    long start = System.currentTimeMillis();
    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "GET " + resource.toString() );
    }
    ClientResponse response = resource.get( ClientResponse.class );
    long end = System.currentTimeMillis();
    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "Took: " + ( end - start ) + " ms" );
    }

    ActionResponseSerializer.verifyNoError( response );

    if ( LOG.isLoggable( Level.FINER ) ) {
      try {
        byte[] content = ByteStreams.toByteArray( response.getEntityInputStream() );
        if ( content.length > DEBUG_MAX_LENGTH ) {
          LOG.finer( "Showing first " + DEBUG_MAX_LENGTH + " bytes:\n" + new String( content ).substring( 0, DEBUG_MAX_LENGTH ) + "..." );
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

  @Nonnull
  public InputStream get( @Nonnull DocId docId, @Nonnull AttachmentId attachmentId ) throws ActionFailedException {
    return get( getDbRoot().path( docId.asString() ).path( attachmentId.asString() ) );
  }

  @Nonnull
  public InputStream query( @Nonnull ViewDescriptor viewDescriptor, @Nullable Options options ) throws ActionFailedException {
    WebResource viewPath = getDbRoot().path( PATH_SEGMENT_DESIGN ).path( viewDescriptor.getDesignDocumentId() ).path( PATH_SEGMENT_VIEW ).path( viewDescriptor.getViewId() );

    if ( options != null ) {
      MultivaluedMap<String, String> params = new MultivaluedMapImpl();
      for ( Map.Entry<String, String> paramEntry : options.getParams().entrySet() ) {
        params.putSingle( paramEntry.getKey(), paramEntry.getValue() );
      }

      viewPath = viewPath.queryParams( params );
    }

    return get( viewPath );
  }
}
