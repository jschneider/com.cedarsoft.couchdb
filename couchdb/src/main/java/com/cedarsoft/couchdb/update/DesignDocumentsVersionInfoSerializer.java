
package com.cedarsoft.couchdb.update;

import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.JacksonParserWrapper;
import com.cedarsoft.version.Version;
import com.cedarsoft.version.VersionException;
import com.cedarsoft.version.VersionRange;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

import javax.annotation.Nonnull;
import java.io.IOException;

public class DesignDocumentsVersionInfoSerializer extends AbstractJacksonSerializer<DesignDocumentsVersionInfo> {

  public static final String PROPERTY_DOCS_VERSION = "designDocumentsVersion";
  public static final String PROPERTY_UPDATEDAT = "updatedAt";
  public static final String PROPERTY_UPDATEDBY = "updatedBy";

  public DesignDocumentsVersionInfoSerializer() {
    super( "design-documents-version-info", VersionRange.from( 1, 0, 0 ).to( 1, 0, 0 ) );
  }

  @Override
  public void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull DesignDocumentsVersionInfo object, @Nonnull Version formatVersion ) throws IOException, JsonProcessingException {
    verifyVersionWritable( formatVersion );
    //version
    serializeTo.writeStringField( PROPERTY_DOCS_VERSION, object.getVersion().format() );
    //updatedAt
    serializeTo.writeNumberField( PROPERTY_UPDATEDAT, object.getUpdatedAt() );
    //updatedBy
    serializeTo.writeStringField( PROPERTY_UPDATEDBY, object.getUpdatedBy() );
  }

  @Nonnull
  @Override
  public DesignDocumentsVersionInfo deserialize( @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws VersionException, IOException, JsonProcessingException {
    verifyVersionReadable( formatVersion );
    JacksonParserWrapper parser = new JacksonParserWrapper( deserializeFrom );

    long updatedAt=-1;
    Version version = null;
    String updatedBy = null;

    while ( parser.nextToken() == JsonToken.FIELD_NAME ) {
      String currentName = parser.getCurrentName();

      if ( currentName.equals( PROPERTY_DOCS_VERSION ) ) {
        parser.nextToken( JsonToken.VALUE_STRING );
        version = Version.parse( deserializeFrom.getText() );
        continue;
      }

      if ( currentName.equals( PROPERTY_UPDATEDAT ) ) {
        parser.nextToken( JsonToken.VALUE_NUMBER_INT );
        updatedAt = deserializeFrom.getLongValue();
        continue;
      }

      if ( currentName.equals( PROPERTY_UPDATEDBY ) ) {
        parser.nextToken( JsonToken.VALUE_STRING );
        updatedBy = deserializeFrom.getText();
        continue;
      }

      throw new IllegalStateException( "Unexpected field reached <" + currentName + ">" );
    }

    parser.verifyDeserialized( version, PROPERTY_VERSION );
    parser.verifyDeserialized( updatedBy, PROPERTY_UPDATEDBY );
    parser.verifyDeserialized( updatedAt, PROPERTY_UPDATEDAT );

    assert updatedBy != null;
    assert version != null;

    parser.ensureObjectClosed();

    //Finally closing element
    parser.ensureObjectClosed();
    //Constructing the deserialized object
    return new DesignDocumentsVersionInfo( version, updatedAt, updatedBy );
  }
}
