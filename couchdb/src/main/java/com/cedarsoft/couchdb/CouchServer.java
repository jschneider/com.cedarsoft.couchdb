package com.cedarsoft.couchdb;

import com.cedarsoft.couchdb.io.RowSerializer;
import com.cedarsoft.couchdb.io.ViewResponseSerializer;
import com.cedarsoft.serialization.jackson.ListSerializer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.jcouchdb.db.Response;
import org.jcouchdb.db.Server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchServer {
  @Nonnull
  public static final String ALL_DBS = "_all_dbs";

  @Nonnull
  private final Client client;
  @Nonnull
  private final ClientFilter[] clientFilters;
  @Nonnull
  private final WebResource root;


  public CouchServer( @Nonnull URI uri, @Nullable ClientFilter... filters ) {
    client = new Client( );
    if ( filters != null ) {
      for ( ClientFilter filter : filters ) {
        if ( filter != null ) {
          client.addFilter( filter );
        }
      }
    }
    this.clientFilters = filters == null ? new ClientFilter[0] : filters.clone( );
    root = client.resource( uri );
  }

  public void deleteDatabase( @Nonnull String dbName ) throws ActionFailedException {
    ClientResponse response = root.path( dbName ).delete( ClientResponse.class );
    ActionResponse.verifyNoError( response );
  }

  public boolean createDatabase( @Nonnull String dbName ) throws ActionFailedException {
    ClientResponse response = root.path( dbName ).put( ClientResponse.class );
    if ( response.getStatus( ) == 201 ) {
      return true;
    }

    ActionResponse.verifyNoError( response );
    return false;
  }

  @Nonnull
  public List<? extends String> listDatabases( ) throws IOException {
    InputStream in = root.path( ALL_DBS ).get( InputStream.class );

    ListSerializer listSerializer = new ListSerializer( );
    List<? extends Object> dbs = listSerializer.deserialize( in );

    return ( List<? extends String> ) dbs;
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
