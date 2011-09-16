package com.cedarsoft.couchdb.io;

import com.cedarsoft.couchdb.DocId;
import com.cedarsoft.couchdb.Revision;
import com.cedarsoft.couchdb.UniqueId;
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
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * This wrapper skips couchdb specific entries ("_id" and "_rev").
 * <p/>
 * Wraps a default serializer.
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 * @param <T> the type
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
  protected void beforeTypeAndVersion( @Nonnull JacksonParserWrapper wrapper ) throws IOException, JsonProcessingException, InvalidTypeException {
    super.beforeTypeAndVersion( wrapper );

    wrapper.nextFieldValue( "_id" );
    final DocId id = new DocId( wrapper.getText() );
    wrapper.nextFieldValue( "_rev" );
    final Revision revision = new Revision( wrapper.getText() );

    current = new UniqueId( id, revision );
  }

  @Nonnull
  @Override
  public T deserialize( @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
    return delegate.deserialize( deserializeFrom, formatVersion );
  }

  @Nullable
  private UniqueId current;

  /**
   * Returns the current unique id
   *
   * @return the current unique id
   *
   * @noinspection NullableProblems
   */
  @Nonnull
  public UniqueId getCurrent() throws IllegalStateException {
    @Nullable final UniqueId copy = current;
    if ( copy == null ) {
      throw new IllegalStateException( "No current id available" );
    }
    return copy;
  }
}
