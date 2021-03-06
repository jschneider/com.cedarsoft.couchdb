package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.core.ActionResponse;
import com.cedarsoft.couchdb.core.CouchDoc;
import com.cedarsoft.couchdb.core.DocId;
import com.cedarsoft.couchdb.core.Key;
import com.cedarsoft.couchdb.core.Options;
import com.cedarsoft.couchdb.core.ViewResponse;
import com.cedarsoft.couchdb.test.utils.foo.Views;
import com.cedarsoft.couchdb.update.DesignDocumentsProvider;
import com.cedarsoft.serialization.jackson.ListSerializer;
import com.cedarsoft.serialization.jackson.NullSerializer;
import com.cedarsoft.serialization.jackson.StringSerializer;
import com.cedarsoft.test.utils.JsonUtils;
import com.cedarsoft.version.Version;
import com.google.common.io.ByteStreams;
import org.junit.*;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class Foo2Test extends CouchTest {

  private Foo.Serializer serializer;

  @Override
  protected DesignDocumentsProvider getDesignDocumentsProvider() {
    @Nullable URL resource = getClass().getResource( "foo/doc1/aView.map.js" );
    assertThat( resource ).isNotNull();
    return new FileBasedDesignDocumentsProvider( resource, Version.valueOf( 1, 0, 0 ) );
  }

  @Before
  public void setUp() throws Exception {
    serializer = new Foo.Serializer();
  }

  @Test
  public void testMultiQuery() throws Exception {
    {
      ViewResponse<List<? extends Object>, Void, Foo> response = db().query( Views.Doc1.A_VIEW, new ListSerializer(), new NullSerializer(), serializer, null );
      assertThat( response.getTotalRows() ).isEqualTo( 0 );
      assertThat( response.getRowObjects() ).hasSize( 0 );
    }

    //Create the objects
    for ( int i = 0; i < 100; i++ ) {
      ActionResponse response = db().put( new CouchDoc<Foo>( new DocId( "foo" + i ), new Foo( i, "desc" + i ) ), serializer );
      assertThat( response.getStatus() ).isEqualTo( 201 );
      assertThat( response.getLocation().getPath() ).endsWith( "/foo" + i );
    }


    //First query - without any options
    {
      ViewResponse<List<? extends Object>, String, Foo> response = db().query( Views.Doc1.A_VIEW, new ListSerializer(), new StringSerializer(), serializer, Options.empty() );
      assertThat( response.getTotalRows() ).isEqualTo( 100 );
      assertThat( response.getOffset() ).isEqualTo( 0 );
      assertThat( response.getRows() ).hasSize( 100 );
      assertThat( response.getRowObjects() ).hasSize( 100 );

      assertThat( response.getRowObjects().get( 0 ).getaValue() ).isSameAs( 0 );
      assertThat( response.getRowObjects().get( 99 ).getaValue() ).isSameAs( 99 );
    }


    //Query with key
    //First query - without any options
    {
      ViewResponse<List<? extends Object>, String, Foo> response = db().query( Views.Doc1.A_VIEW, new ListSerializer(), new StringSerializer(), serializer, Options.empty().startKey( Key.array( 89, "" ) ) );
      assertThat( response.getTotalRows() ).isEqualTo( 100 );
      assertThat( response.getOffset() ).isEqualTo( 89 );
      assertThat( response.getRows() ).hasSize( 11 );
      assertThat( response.getRowObjects() ).hasSize( 11 );

      assertThat( response.getRowObjects().get( 0 ).getaValue() ).isSameAs( 89 );
      assertThat( response.getRowObjects().get( 10 ).getaValue() ).isSameAs( 99 );
    }

    {
      ViewResponse<List<? extends Object>, String, Foo> response = db().query( Views.Doc1.A_VIEW, new ListSerializer(), new StringSerializer(), serializer, Options.empty().startKey( Key.array( 89, "" ) ).descending( true ) );
      assertThat( response.getTotalRows() ).isEqualTo( 100 );
      assertThat( response.getOffset() ).isEqualTo( 11 );
      assertThat( response.getRows() ).hasSize( 89 );
      assertThat( response.getRowObjects() ).hasSize( 89 );

      assertThat( response.getRowObjects().get( 0 ).getaValue() ).isSameAs( 88 );
      assertThat( response.getRowObjects().get( 88 ).getaValue() ).isSameAs( 0 );
    }



    //What happens if there is not serializer added?
    {
      ViewResponse<List<? extends Object>, String, Void> response = db().query( Views.Doc1.A_VIEW, new ListSerializer(), new StringSerializer(), null );
      assertThat( response.getTotalRows() ).isEqualTo( 100 );
      assertThat( response.getOffset() ).isEqualTo( 0 );
      assertThat( response.getRows() ).hasSize( 100 );
      assertThat( response.getRows().get( 0 ).getValue() ).isEqualTo( "desc0" );

      assertThat( response.getRowObjects() ).hasSize( 100 );
      assertThat( response.getRowObjects().get( 0 ) ).isNull(  );
      assertThat( response.getRowObjects().get( 99 ) ).isNull(  );
    }

    //But the docs are included?
    {
      try {
        db().query( Views.Doc1.A_VIEW, new ListSerializer(), new StringSerializer(), new Options().includeDocs( true ) );
        fail("Where is the Exception");
      } catch ( IllegalArgumentException e ) {
        assertThat( e ).hasMessage( "include_docs is not supported without a doc serializer" );
      }
    }
  }

  @Test
  public void testSingleQuery() throws Exception {
    {
      ViewResponse<List<? extends Object>, Void, Foo> response = db().query( Views.Doc1.A_VIEW, new ListSerializer(), new NullSerializer(), serializer, Options.empty() );
      assertThat( response.getTotalRows() ).isEqualTo( 0 );
      assertThat( response.getRowObjects() ).hasSize( 0 );
    }


    {
      ActionResponse response = db().put( new CouchDoc<Foo>( new DocId( "asdfasdf" ), new Foo( 123, "helloyou" ) ), serializer );
      assertThat( response.getStatus() ).isEqualTo( 201 );
      assertThat( response.getLocation().getPath() ).endsWith( "/asdfasdf" );
    }


    InputStream inputStream = db().query( Views.Doc1.A_VIEW, Options.empty() );
    JsonUtils.assertJsonEquals( "{\"total_rows\":1,\"offset\":0,\"rows\":[\n" +
                                  "{\"id\":\"asdfasdf\",\"key\":[123,\"helloyou\"],\"value\":\"helloyou\"}\n" +
                                  "]}", new String( ByteStreams.toByteArray( inputStream ) ) );


    {
      ViewResponse<List<? extends Object>, String, Foo> response = db().query( Views.Doc1.A_VIEW, new ListSerializer(), new StringSerializer(), serializer, Options.empty() );
      assertThat( response.getTotalRows() ).isEqualTo( 1 );
      assertThat( response.getOffset() ).isEqualTo( 0 );
      assertThat( response.getRows() ).hasSize( 1 );
      assertThat( response.getRowObjects() ).hasSize( 1 );

      Foo foo = response.getRowObjects().get( 0 );
      assertThat( foo.getaValue() ).isEqualTo( 123 );
      assertThat( foo.getDescription() ).isEqualTo( "helloyou" );

      assertThat( response.getRows().get( 0 ).getValue() ).isEqualTo( "helloyou" );
      assertThat( response.getRows().get( 0 ).getKey() ).hasSize( 2 );
      assertThat( response.getRows().get( 0 ).getKey().get( 0 ) ).isEqualTo( 123 );
      assertThat( response.getRows().get( 0 ).getKey().get( 1 ) ).isEqualTo( "helloyou" );

      assertThat( response.getRows().get( 0 ).getId().asString() ).isEqualTo( "asdfasdf" );
    }
  }
}
