
package com.cedarsoft.couchdb.update;

import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import com.cedarsoft.serialization.jackson.JacksonParserWrapper;
import com.cedarsoft.version.Version;
import com.cedarsoft.version.VersionException;
import com.cedarsoft.version.VersionRange;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;

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
    //version
    parser.nextFieldValue( PROPERTY_DOCS_VERSION );
    Version version = Version.parse( deserializeFrom.getText() );
    //updatedAt
    parser.nextFieldValue( PROPERTY_UPDATEDAT );
    long updatedAt = deserializeFrom.getLongValue();
    //updatedBy
    parser.nextFieldValue( PROPERTY_UPDATEDBY );
    String updatedBy = deserializeFrom.getText();
    //Finally closing element
    parser.closeObject();
    //Constructing the deserialized object
    return new DesignDocumentsVersionInfo( version, updatedAt, updatedBy );
  }

}
