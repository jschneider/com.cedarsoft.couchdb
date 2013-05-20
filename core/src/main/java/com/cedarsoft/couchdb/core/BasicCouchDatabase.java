package com.cedarsoft.couchdb.core;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public abstract class BasicCouchDatabase {
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
  public static final int DEBUG_MAX_LENGTH = 1024;
  @Nonnull
  protected static final Logger LOG = Logger.getLogger( BasicCouchDatabase.class.getName() );
  @Nonnull
  protected final WebResource dbRoot;

  public BasicCouchDatabase( @Nonnull WebResource dbRoot ) {
    this.dbRoot = dbRoot;
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

  @Override
  public String toString() {
    return "CouchDatabase{" +
      "dbRoot=" + getDbRoot() +
      '}';
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
    WebResource.Builder path = getDbRoot()
      .path( docId.asString() )
      .path( attachmentId.asString() )
      .type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "HEAD " + path.toString() );
    }

    return path.head();
  }

  /**
   * Returns the head for the given doc
   *
   * @param docId the doc
   * @return the head for the given doc id
   */
  @Nonnull
  public ClientResponse getHead( @Nonnull DocId docId ) {
    WebResource.Builder path = getDbRoot().path( docId.asString() ).type( JSON_TYPE ).accept( JSON_TYPE );

    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "HEAD " + path.toString() );
    }

    return path.head();
  }

  /**
   * Returns the document
   *
   * @param id the id
   * @return the view as stream
   */
  @Nonnull
  public InputStream get( @Nonnull DocId id ) throws ActionFailedException{
    return get( dbRoot.path( id.asString() ) );
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
  public abstract InputStream get( @Nonnull DocId docId, @Nonnull AttachmentId attachmentId ) throws ActionFailedException;

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
  public abstract InputStream query( @Nonnull ViewDescriptor viewDescriptor, @Nullable Options options ) throws ActionFailedException;

  @Nonnull
  public abstract InputStream get( @Nonnull WebResource resource ) throws ActionFailedException;

  @Nonnull
  public abstract Revision getRev( @Nonnull DocId docId ) throws ActionFailedException;
}
