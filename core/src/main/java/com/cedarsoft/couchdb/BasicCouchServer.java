package com.cedarsoft.couchdb;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.annotation.Nonnull;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class BasicCouchServer {
  @Nonnull
  public static final String ALL_DBS = "_all_dbs";
  @Nonnull
  protected final WebResource root;

  public BasicCouchServer( @Nonnull WebResource root ) {
    this.root = root;
  }

  @Nonnull
  public ClientResponse get( @Nonnull String uri ) {
    return root.path( uri ).get( ClientResponse.class );
  }

  @Nonnull
  public ClientResponse put( @Nonnull String uri, @Nonnull byte[] bytes, @Nonnull String mediaType ) {
    return root.path( uri ).type( mediaType ).put( ClientResponse.class, bytes );
  }
}
