package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.io.CouchSerializerWrapper;
import com.cedarsoft.version.VersionException;
import org.codehaus.jackson.JsonParseException;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DocWrappingSerializerTest {
  private URL res;

  @Before
  public void setUp() throws Exception {
    res = getClass().getResource( "bar.json" );
  }

  @Test
  public void testException() throws Exception {
    try {
      new Bar.Serializer().deserialize( res.openStream() );
      fail( "Where is the Exception" );
    } catch ( JsonParseException e ) {
      assertThat( e.getMessage() ).startsWith( "Invalid field. Expected <@type> but was <_id>" );
    }
  }

  @Test
  public void testWrapped() throws Exception {
    CouchSerializerWrapper<Bar> serializer = new CouchSerializerWrapper<Bar>( new Bar.Serializer() );
    InputStream in = res.openStream();
    Bar bar = serializer.deserialize( in );

    assertThat( bar.getValue() ).isEqualTo( 7 );
    assertThat( bar.getDescription() ).isEqualTo( "hey" );
  }
}
