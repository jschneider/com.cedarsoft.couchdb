package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.ActionFailedException;
import com.cedarsoft.couchdb.DocId;
import com.google.common.base.Charsets;
import org.apache.commons.io.input.TeeInputStream;
import org.junit.*;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class BetterLogMessagesTest extends CouchTest {
  @Test
  public void testError() throws Exception {
    try {
      db().get( new DocId( "DoesNotExist" ) );
      fail( "Where is the Exception" );
    } catch ( ActionFailedException e ) {
      assertThat( e.getStatus() ).isEqualTo( 404 );
      assertThat( e.getError() ).isEqualTo( "not_found" );
      assertThat( e.getReason() ).isEqualTo( "missing" );

      assertThat( e.getMessage() ).isEqualTo( "404 not_found: missing" );
      assertThat( e.getLocalizedMessage() ).isEqualTo( "404 not_found: missing" );
      assertThat( new String(e.getRaw(), Charsets.UTF_8).trim() ).isEqualTo( "{\"error\":\"not_found\",\"reason\":\"missing\"}" );
    }
  }

  @Test
  public void testTeaInput() throws Exception {
    ByteArrayOutputStream targetOut = new ByteArrayOutputStream();
    try ( InputStream inputStream = new TeeInputStream( new ByteArrayInputStream( "content".getBytes() ), targetOut ) ) {
      assertThat( targetOut.toString() ).isEqualTo( "" );
      assertThat( inputStream.read() ).isEqualTo( 'c' );
      assertThat( targetOut.toString() ).isEqualTo( "c" );
      assertThat( inputStream.read() ).isEqualTo( 'o' );
      assertThat( targetOut.toString() ).isEqualTo( "co" );
      assertThat( inputStream.read() ).isEqualTo( 'n' );
      assertThat( inputStream.read() ).isEqualTo( 't' );
      assertThat( inputStream.read() ).isEqualTo( 'e' );
      assertThat( inputStream.read() ).isEqualTo( 'n' );
      assertThat( targetOut.toString() ).isEqualTo( "conten" );
      assertThat( inputStream.read() ).isEqualTo( 't' );
      assertThat( targetOut.toString() ).isEqualTo( "content" );
      assertThat( inputStream.read() ).isEqualTo( -1 );
      assertThat( targetOut.toString() ).isEqualTo( "content" );
    }
  }
}
