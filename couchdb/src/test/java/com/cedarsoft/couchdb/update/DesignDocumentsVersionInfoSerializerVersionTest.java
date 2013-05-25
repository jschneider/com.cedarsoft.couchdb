
package com.cedarsoft.couchdb.update;

import com.cedarsoft.serialization.Serializer;
import com.cedarsoft.serialization.test.utils.AbstractJsonVersionTest2;
import com.cedarsoft.serialization.test.utils.VersionEntry;
import com.cedarsoft.version.Version;
import org.junit.experimental.theories.*;

import javax.annotation.Nonnull;

import static org.fest.assertions.Assertions.assertThat;

public class DesignDocumentsVersionInfoSerializerVersionTest extends AbstractJsonVersionTest2<DesignDocumentsVersionInfo> {

  @DataPoint
  public static final VersionEntry ENTRY1 = DesignDocumentsVersionInfoSerializerVersionTest.create( Version.valueOf( 1, 0, 0 ), DesignDocumentsVersionInfoSerializerVersionTest.class.getResource( "DesignDocumentsVersionInfo_1.0.0_1.json" ) );

  @Nonnull
  @Override
  protected Serializer<DesignDocumentsVersionInfo> getSerializer() throws Exception {
    return new DesignDocumentsVersionInfoSerializer();
  }

  @Override
  protected void verifyDeserialized( @Nonnull DesignDocumentsVersionInfo deserialized, @Nonnull Version version ) throws Exception {
    assertThat( deserialized.getVersion() ).isEqualTo( new Version( 1, 2, 3 ) );
    assertThat( deserialized.getUpdatedAt() ).isEqualTo( 43L );
    assertThat( deserialized.getUpdatedBy() ).isEqualTo( "Mark Mustermann@moria2000" );
  }
}
