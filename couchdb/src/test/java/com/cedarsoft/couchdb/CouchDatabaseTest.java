package com.cedarsoft.couchdb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.junit.*;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchDatabaseTest {
  @Test
  public void testCons() throws URISyntaxException {
    WebResource resource1 = new Client().resource( "http://" + "localhost" + ":" + 8080 ).path( "daPath" );
    WebResource resource2 = new Client().resource( new URI( "http://localhost:8080/daPath" ) );

    assertEquals( resource1, resource2 );
  }

  @Test
  public void testDbNameUri() throws Exception {
    URI serverUri = new URI( "http://localhost:8080" );
    String dbName = "daDb";

    assertEquals( "localhost:8080", serverUri.getAuthority() );
    assertEquals( "localhost", serverUri.getHost() );
    assertEquals( "", serverUri.getPath() );
    assertEquals( null, serverUri.getFragment() );
    assertEquals( 8080, serverUri.getPort() );
    assertEquals( "", serverUri.getRawPath() );
    assertEquals( "http", serverUri.getScheme() );
    assertEquals( null, serverUri.getUserInfo() );

    assertEquals( "http://localhost:8080/daDb", serverUri.resolve( "/" + dbName ).toString() );
  }

  @Test
  public void testUri() throws Exception {
    URI serverUri = new URI( "http://localhost:8080/daDb" );
    assertEquals( "localhost:8080", serverUri.getAuthority() );
    assertEquals( "localhost", serverUri.getHost() );
    assertEquals( "/daDb", serverUri.getPath() );
    assertEquals( null, serverUri.getFragment() );
    assertEquals( 8080, serverUri.getPort() );
    assertEquals( "/daDb", serverUri.getRawPath() );
    assertEquals( "http", serverUri.getScheme() );
    assertEquals( null, serverUri.getUserInfo() );
  }
}
