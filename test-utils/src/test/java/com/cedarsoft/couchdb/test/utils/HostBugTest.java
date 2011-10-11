package com.cedarsoft.couchdb.test.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jndi.toolkit.url.Uri;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jcouchdb.db.Server;
import org.jcouchdb.db.ServerImpl;
import org.junit.*;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class HostBugTest {
  @Test
  public void testServerUri( ) throws Exception {
    Uri serverURI = new Uri( "http://couchdb.cedarsoft.com/" );
    assertThat( serverURI.getHost( ) ).isEqualTo( "couchdb.cedarsoft.com" );
    assertThat( serverURI.getPort( ) ).isEqualTo( -1 );
  }

  @Test
  public void testManua( ) throws Exception {
    Client client = new Client( );
    client.addFilter( new HTTPBasicAuthFilter( "hudson", "lkiah54lkasfd" ) );

    WebResource resource = client.resource( "http://couchdb.cedarsoft.com/asdf" );
    resource.put( );
  }
}
