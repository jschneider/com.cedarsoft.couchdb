package com.cedarsoft.couchdb.test;

import com.cedarsoft.JsonUtils;
import com.cedarsoft.couchdb.AttachmentId;
import com.cedarsoft.couchdb.CouchDbTest;
import com.cedarsoft.couchdb.CreationResponse;
import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.couchdb.io.RawCouchDocSerializer;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.*;

import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class AttachmentsTest extends CouchDbTest {
  private RawCouchDocSerializer serializer;

  @Before
  public void setup() throws URISyntaxException {
    serializer = new RawCouchDocSerializer();
  }

  @Test
  public void testManually() throws Exception {
    WebResource dbRoot = db.getDbRoot();

    String docId = "daDocId";

    {
      ClientResponse response = dbRoot.path( docId ).path( "test_data.xml" ).type( MediaType.APPLICATION_XML_TYPE ).put( ClientResponse.class, getClass().getResourceAsStream( "test_data.xml" ) );
      assertEquals( "{\"ok\":true,\"id\":\"daDocId\",\"rev\":\"1-4b8635c26c5b91bd2bc658ed866c727a\"}", response.getEntity( String.class ).trim() );
      assertEquals( 201, response.getStatus() );

      String doc = dbRoot.path( docId ).get( String.class );
      JsonUtils.assertJsonEquals( getClass().getResource( "doc_with_attachment.json" ), doc );
    }

    {
      ClientResponse response = dbRoot.path( docId ).path( "test_data2.xml" ).queryParam( "rev", "1-4b8635c26c5b91bd2bc658ed866c727a" ).type( MediaType.APPLICATION_XML_TYPE ).put( ClientResponse.class, getClass().getResourceAsStream( "test_data2.xml" ) );
      assertEquals( "{\"ok\":true,\"id\":\"daDocId\",\"rev\":\"2-929f2959f8e81ed6b6c7784bee926065\"}", response.getEntity( String.class ).trim() );
      assertEquals( 201, response.getStatus() );

      String doc = dbRoot.path( docId ).get( String.class );
      JsonUtils.assertJsonEquals( getClass().getResource( "doc_with_attachment2.json" ), doc );
    }
  }

  @Test
  public void testDoc2() throws Exception {
    {
      CreationResponse response = db.putAttachment( new DocId( "daId" ), null, new AttachmentId( "data1" ), MediaType.APPLICATION_XML_TYPE, getClass().getResourceAsStream( "test_data.xml" ) );
      assertEquals( "daId", response.getId().asString() );
      assertEquals( "1-3499ac5c79c87e384eb9178bd181c65d", response.getRev().asString() );
    }

    //Add a second attachment
    {
      CreationResponse response = db.putAttachment( new DocId( "daId" ), new Revision( "1-3499ac5c79c87e384eb9178bd181c65d" ), new AttachmentId( "data2" ), MediaType.APPLICATION_XML_TYPE, getClass().getResourceAsStream( "test_data2.xml" ) );
      assertEquals( "daId", response.getId().asString() );
      assertEquals( "2-6f5ea432644e1d9ca07990e8215be17f", response.getRev().asString() );
    }
  }
}
