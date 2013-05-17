package com.cedarsoft.couchdb;

import com.cedarsoft.serialization.jackson.ListSerializer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchUtils {
  private CouchUtils() {
  }

  @Nonnull
  public static Collection<? extends String> listDatabases( @Nonnull CouchServer couchServer ) throws IOException {
    try ( InputStream in = couchServer.get( CouchServer.ALL_DBS ).getEntityInputStream() ) {
      ListSerializer listSerializer = new ListSerializer();
      List<? extends Object> dbs = listSerializer.deserialize( in );

      return ( Collection<? extends String> ) dbs;
    }
  }
}
