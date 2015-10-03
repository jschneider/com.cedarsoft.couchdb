package com.cedarsoft.couchdb.core;

import org.junit.*;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class OptionsTest {
  @Test
  public void testBasic() throws Exception {
    Options options = new Options();
    assertThat( options.limit( 10 ).toQuery() ).isEqualTo( "?limit=10" );
  }

  @Test
  public void testWithKeys() throws Exception {
    Key key = Key.array( "a", "b", "c" );
    assertThat( key.getJson() ).isEqualTo( "[\"a\",\"b\",\"c\"]" );
    assertThat( new Options().startKey( key ).toQuery() ).isEqualTo( "?startkey=%5B%22a%22%2C%22b%22%2C%22c%22%5D" );
  }
}
