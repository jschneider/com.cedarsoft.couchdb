package com.cedarsoft.couchdb.io;

import org.junit.*;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class MediaTypeTest {
  @Test
  public void testMediaType() throws Exception {
    assertEquals( MediaType.APPLICATION_XML_TYPE, MediaType.valueOf( "application/xml" ) );
    assertEquals( "application/collustra+custom", MediaType.valueOf( "application/collustra+custom" ).toString() );
  }

  @Test
  public void testName() throws Exception {
    assertEquals( "application/vnd.collustra+xml", MediaType.valueOf( "application/vnd.collustra+xml" ).toString() );
    assertEquals( "application", MediaType.valueOf( "application/vnd.collustra+xml" ).getType() );
    assertEquals( "vnd.collustra+xml", MediaType.valueOf( "application/vnd.collustra+xml" ).getSubtype() );
    assertEquals( "{}", MediaType.valueOf( "application/vnd.collustra+xml" ).getParameters().toString() );
  }

  @Test
  public void testParameters() throws Exception {
    assertEquals( 2, MediaType.valueOf( "application/vnd.collustra+xml;done=true;version=1.0.1" ).getParameters().size() );
    assertEquals( "{done=true, version=1.0.1}", MediaType.valueOf( "application/vnd.collustra+xml;done=true;version=1.0.1" ).getParameters().toString() );
  }
}
