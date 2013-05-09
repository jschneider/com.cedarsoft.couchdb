
package com.cedarsoft.couchdb.update;

import com.cedarsoft.serialization.Serializer;
import com.cedarsoft.serialization.test.utils.AbstractJsonSerializerTest2;
import com.cedarsoft.serialization.test.utils.Entry;
import com.cedarsoft.version.Version;
import org.junit.experimental.theories.*;

import javax.annotation.Nonnull;

public class DesignDocumentsVersionInfoSerializerTest extends AbstractJsonSerializerTest2<DesignDocumentsVersionInfo> {

  @DataPoint
  public static final Entry<? extends DesignDocumentsVersionInfo> ENTRY1 = DesignDocumentsVersionInfoSerializerTest.create( new DesignDocumentsVersionInfo(
    new Version( 1, 2, 3 ), 43L, "Mark Mustermann@moria2000" ), DesignDocumentsVersionInfoSerializerTest.class.getResource( "DesignDocumentsVersionInfo_1.0.0_1.json" ) );

  @Nonnull
  @Override
  protected Serializer<DesignDocumentsVersionInfo> getSerializer() throws Exception {
    return new DesignDocumentsVersionInfoSerializer();
  }

}
