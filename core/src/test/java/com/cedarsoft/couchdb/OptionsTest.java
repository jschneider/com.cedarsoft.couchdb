package com.cedarsoft.couchdb;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.fest.assertions.Assertions;
import org.junit.*;

import java.io.StringWriter;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class OptionsTest {
  @Test
  public void testBasic() throws Exception {
    Options options = new Options();
    Assertions.assertThat( options.limit( 10 ).toQuery() ).isEqualTo( "?limit=10" );
  }

  @Test
  public void testWithKeys() throws Exception {
    Key key = Key.array( "a", "b", "c" );
    Assertions.assertThat( key.getJson() ).isEqualTo( "[\"a\",\"b\",\"c\"]" );
    Assertions.assertThat( new Options().startKey( key ).toQuery() ).isEqualTo( "?startkey=%5B%22a%22%2C%22b%22%2C%22c%22%5D" );
  }
}
