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
}
