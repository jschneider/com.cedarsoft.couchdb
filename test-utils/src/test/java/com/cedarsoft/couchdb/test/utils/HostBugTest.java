package com.cedarsoft.couchdb.test.utils;

import com.sun.jndi.toolkit.url.Uri;
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
}
