package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.ActionResponse;
import com.cedarsoft.couchdb.DocId;
import org.junit.*;

import java.io.ByteArrayInputStream;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class PostTest extends CouchTest {
  @Test
  public void testPost() throws Exception {
    String content = "{}";

    {
      ActionResponse response = db().put( new DocId( "dadocid" ), new ByteArrayInputStream( content.getBytes() ) );
      assertThat( response.getId().asString() ).isEqualTo( "dadocid" );
    }

    ActionResponse response = db().post( new ByteArrayInputStream( content.getBytes() ) );
    assertThat( response.getStatus() ).isEqualTo( 201 );
    assertThat( response.getId().asString() ).hasSize( 32 );
    assertThat( response.getRev().asString() ).startsWith( "1-" );
  }
}
