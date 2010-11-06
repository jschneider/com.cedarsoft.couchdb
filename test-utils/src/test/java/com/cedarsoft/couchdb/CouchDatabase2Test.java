package com.cedarsoft.couchdb;

import com.cedarsoft.CanceledException;
import com.cedarsoft.JsonUtils;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.rules.*;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchDatabase2Test extends CouchDbTest {
  @NonNls
  private static final String REV = "1-8908180fecd9b17657889f91973f89eb";

  private CouchDocSerializer couchDocSerializer;
  private final Bar.Serializer serializer = new Bar.Serializer();

  @Before
  public void setup() throws URISyntaxException {
    couchDocSerializer = new CouchDocSerializer();
  }

  @Test
  public void testManually() throws Exception {
    CreationResponse response = db.put( new CouchDoc<Bar>( "daId", new Bar( 7, "hey" ) ), serializer );
    assertEquals( "daId", response.getId() );
    assertEquals( REV, response.getRev() );

    {
      byte[] read = ByteStreams.toByteArray( db.get( "daId" ) );
      JsonUtils.assertJsonEquals( getClass().getResource( "bar.json" ), new String( read ) );

      CouchDoc<Bar> doc = couchDocSerializer.deserialize( serializer, new ByteArrayInputStream( read ) );
      assertEquals( "daId", doc.getId() );
      assertEquals( REV, doc.getRev() );
      assertEquals( 7, doc.getObject().getValue() );
      assertEquals( "hey", doc.getObject().getDescription() );
    }
  }

  @Test
  public void testCreateGetDelete() throws Exception {
    {
      CreationResponse response = db.put( new CouchDoc<Bar>( "daId", new Bar( 7, "hey" ) ), serializer );

      assertEquals( "daId", response.getId() );
      assertEquals( REV, response.getRev() );
    }

    {
      CouchDoc<Bar> doc = db.get( "daId", serializer );
      assertEquals( "daId", doc.getId() );
      assertEquals( REV, doc.getRev() );
      assertEquals( 7, doc.getObject().getValue() );
    }

    db.delete( "daId", REV );


    //Check deleted
    try {
      db.get( "daId", serializer );
    } catch ( UniformInterfaceException e ) {
      assertEquals( 404, e.getResponse().getStatus() );
      assertEquals( "{\"error\":\"not_found\",\"reason\":\"deleted\"}", e.getResponse().getEntity( String.class ).trim() );
    }
  }

  @Test
  public void testUpdated() throws Exception {
    //Create
    {
      db.put( new CouchDoc<Bar>( "daId", new Bar( 7, "hey" ) ), serializer );
    }

    //fetch
    {
      CouchDoc<Bar> doc = db.get( "daId", serializer );
      Bar bar = doc.getObject();

      assertEquals( "hey", bar.getDescription() );
      assertEquals( 7, bar.getValue() );

      bar.setDescription( "updatedDescription" );
      bar.setValue( 42 );

      //Update
      db.putUpdated( doc, serializer );
    }

    //fetch2
    {
      CouchDoc<Bar> doc = db.get( "daId", serializer );
      Bar bar = doc.getObject();

      assertEquals( "updatedDescription", bar.getDescription() );
      assertEquals( 42, bar.getValue() );

      bar.setDescription( "updatedDescription2" );
      bar.setValue( 11 );

      //Update
      db.putUpdated( doc, serializer );
    }

    {
      CouchDoc<Bar> doc = db.get( "daId", serializer );
      Bar bar = doc.getObject();

      assertEquals( "updatedDescription2", bar.getDescription() );
      assertEquals( 11, bar.getValue() );
    }

    //Invalid updated without ref
    expectedException.expect( CreationFailedException.class );
    expectedException.expectMessage( "conflict: Document update conflict." );

    db.put( new CouchDoc<Bar>( "daId", new Bar( 1, "should not work!" ) ), serializer );
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @NotNull
  @Override
  protected URL getViewResource() {
    throw new CanceledException();
  }
}
