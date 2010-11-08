package com.cedarsoft.couchdb.io;

import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.RawCouchDoc;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class RawCouchDocSerializer {
  @NonNls
  public static final String PROPERTY_ID = "_id";
  @NonNls
  public static final String PROPERTY_REV = "_rev";

  @NotNull
  public byte[] serialize( @NotNull RawCouchDoc info ) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serialize( info, out );
    return out.toByteArray();
  }

  public void serialize( @NotNull RawCouchDoc doc, @NotNull OutputStream out ) throws IOException {
    JsonGenerator generator = createJsonGenerator( out );
    try {
      serialize( doc, generator );
    } finally {
      generator.close();
    }
  }

  public void serialize( @NotNull RawCouchDoc doc, @NotNull JsonGenerator generator ) throws IOException {
    generator.writeStartObject();
    serializeIdAndRev( generator, doc );
    generator.writeEndObject();
  }

  public void serializeIdAndRev( @NotNull JsonGenerator serializeTo, @NotNull RawCouchDoc doc ) throws IOException, JsonProcessingException {
    serializeTo.writeStringField( PROPERTY_ID, doc.getId().asString() );

    Revision rev = doc.getRev();
    if ( rev != null ) {
      serializeTo.writeStringField( PROPERTY_REV, rev.asString() );
    }
  }

  @NotNull
  public RawCouchDoc deserialize( @NotNull InputStream in ) throws IOException {
    try {
      JsonParser parser = createJsonParser( in );
      RawCouchDoc doc = deserialize( parser );

      AbstractJacksonSerializer.ensureParserClosed( parser );
      return doc;
    } catch ( InvalidTypeException e ) {
      throw new IOException( "Could not parse due to " + e.getMessage(), e );
    }
  }

  @NotNull
  public RawCouchDoc deserialize( @NotNull JsonParser parser ) throws IOException, InvalidTypeException {
    AbstractJacksonSerializer.nextToken( parser, JsonToken.START_OBJECT );

    AbstractJacksonSerializer.nextFieldValue( parser, PROPERTY_ID );
    String id = parser.getText();

    AbstractJacksonSerializer.nextFieldValue( parser, PROPERTY_REV );
    String rev = parser.getText();

    parser.nextToken();

    AbstractJacksonSerializer.ensureObjectClosed( parser );
    return new RawCouchDoc( new DocId( id ), new Revision( rev ) );
  }

  @NotNull
  protected JsonParser createJsonParser( @NotNull InputStream in ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    return jsonFactory.createJsonParser( in );
  }

  @NotNull
  protected JsonGenerator createJsonGenerator( @NotNull OutputStream out ) throws IOException {
    JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
    return jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );
  }
}
