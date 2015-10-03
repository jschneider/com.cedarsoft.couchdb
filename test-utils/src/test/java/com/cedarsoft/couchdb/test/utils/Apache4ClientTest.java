package com.cedarsoft.couchdb.test.utils;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.junit.*;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class Apache4ClientTest {

  public static final String URI = "http://www.random.org/integers/?num=10&min=1&max=100000&col=1&base=10&format=plain&rnd=new";

  @Test
  public void testParallelApache4Client() throws Exception {
    ClientConnectionManager connectionManager = new ThreadSafeClientConnManager();

    DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
    config.getProperties().put( ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager );

    ApacheHttpClient4 client = ApacheHttpClient4.create( config );

    WebResource resource = client.resource( URI );

    ClientResponse clientResponse = resource.get( ClientResponse.class );
    assertThat( clientResponse ).isNotNull();

    ClientResponse response2 = resource.get( ClientResponse.class );
    assertThat( clientResponse.getEntity( String.class ) ).isNotEqualTo( response2.getEntity( String.class ) );
  }

  @Test
  public void testParallelApache4ClientSingleThreadedManager() throws Exception {
    ClientConnectionManager connectionManager = new SingleClientConnManager(  );

    DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
    config.getProperties().put( ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager );

    ApacheHttpClient4 client = ApacheHttpClient4.create( config );

    WebResource resource = client.resource( URI );

    ClientResponse clientResponse = resource.get( ClientResponse.class );
    assertThat( clientResponse ).isNotNull();

    try {
      resource.get( ClientResponse.class );
      fail( "Where is the Exception" );
    } catch ( ClientHandlerException e ) {
      assertThat( e.getCause() ).hasMessage( "Invalid use of SingleClientConnManager: connection still allocated.\n" +
                                               "Make sure to release the connection before allocating another one." );
    }
  }

  @Test
  public void testParallelDefaultClient() throws Exception {
    Client client = Client.create();
    assertThat( client ).isInstanceOf( Client.class );

    WebResource resource = client.resource( URI );

    ClientResponse response1 = resource.get( ClientResponse.class );
    assertThat( response1 ).isNotNull();

    ClientResponse response2 = resource.get( ClientResponse.class );

    String content2 = response2.getEntity( String.class );
    String content1 = response1.getEntity( String.class );

    assertThat( content1 ).isNotEqualTo( content2 );
  }
}