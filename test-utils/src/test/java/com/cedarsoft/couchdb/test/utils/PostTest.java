package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.ActionResponse;
import com.cedarsoft.couchdb.CouchDoc;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.RawCouchDoc;
import com.cedarsoft.test.utils.AssertUtils;
import com.cedarsoft.test.utils.JsonUtils;
import com.google.common.io.ByteStreams;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class PostTest extends CouchTest {

  public static final String CONTENT = "{}";

  @Test
  public void testPost() throws Exception {

    {
      ActionResponse response = db().put( new DocId( "dadocid" ), new ByteArrayInputStream( CONTENT.getBytes() ) );
      assertThat( response.getId().asString() ).isEqualTo( "dadocid" );
    }

    ActionResponse response = db().post( new ByteArrayInputStream( CONTENT.getBytes() ) );
    assertThat( response.getStatus() ).isEqualTo( 201 );
    assertThat( response.getId().asString() ).hasSize( 32 );
    assertThat( response.getRev().asString() ).startsWith( "1-" );
  }

  @Test
  public void testMoreComplex() throws Exception {
    Foo foot = new Foo( 2, "asdf" );
    {
      CouchDoc<Foo> doc = new CouchDoc<Foo>( new DocId( "foo1" ), foot );
      ActionResponse response = db().put( doc, new Foo.Serializer() );

      assertThat( response.getStatus() ).isEqualTo( 201 );
    }
  }
}
