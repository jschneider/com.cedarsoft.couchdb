package com.cedarsoft.couchdb.io;

import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.InvalidTypeException;
import com.cedarsoft.serialization.jackson.test.compatible.JacksonParserWrapper;
import com.cedarsoft.version.Version;
import com.cedarsoft.version.VersionException;
import com.cedarsoft.version.VersionRange;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * This wrapper skips couchdb specific entries ("_id" and "_rev").
 * <p/>
 * Wraps a default serializer.
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchSerializerWrapper<T> extends AbstractJacksonSerializer<T> {
  @Nonnull
  private final AbstractJacksonSerializer<T> delegate;

  public CouchSerializerWrapper( @Nonnull AbstractJacksonSerializer<T> delegate ) {
    super( delegate.getType(), delegate.getFormatVersionRange() );
    if ( !delegate.isObjectType() ) {
      throw new IllegalStateException( "Not supported for object type serializer: " + delegate.getClass().getName() );
    }

    this.delegate = delegate;
  }

  /**
   * Serialization is only supported for test cases. This method just delegates everything
   *
   * @param serializeTo   serialize to
   * @param object        the object
   * @param formatVersion the format version
   * @throws IOException
   * @throws VersionException
   * @throws JsonProcessingException
   */
  @Override
  public void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull T object, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
    delegate.serialize( serializeTo, object, formatVersion );
  }

  @Override
  protected void beforeTypeAndVersion( @Nonnull JacksonParserWrapper wrapper ) throws IOException, JsonProcessingException, InvalidTypeException{
    super.beforeTypeAndVersion( wrapper );

    wrapper.nextFieldValue( "_id" );
    wrapper.nextFieldValue( "_rev" );
  }

  @Override
  @Nonnull
  public T deserialize( @Nonnull JsonParser parser ) throws IOException, JsonProcessingException, InvalidTypeException {
    JacksonParserWrapper wrapper = new JacksonParserWrapper( parser );

    Version version;
    wrapper.nextToken( JsonToken.START_OBJECT );

    //Couchdb specific stuff...
    wrapper.nextFieldValue( "_id" );
    wrapper.nextFieldValue( "_rev" );

    wrapper.nextFieldValue( PROPERTY_TYPE );
    String readNs = parser.getText();
    verifyType( readNs );
    wrapper.nextFieldValue( PROPERTY_VERSION );
    version = Version.parse( parser.getText() );
    verifyVersionReadable( version );

    T deserialized = deserialize( parser, version );

    ensureObjectClosed( parser );

    return deserialized;
  }


  @Nonnull
  @Override
  public T deserialize( @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
    return delegate.deserialize( deserializeFrom, formatVersion );
  }
}
