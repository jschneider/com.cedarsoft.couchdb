package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.core.CouchDbException;
import com.cedarsoft.couchdb.CouchServer;
import com.cedarsoft.couchdb.CouchUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 * @noinspection UseOfSystemOutOrSystemErr
 */
public class DeleteCouchUnitDbs {
  private DeleteCouchUnitDbs() {
  }

  public static void main( String... args ) throws IOException, URISyntaxException, CouchDbException {
    CouchDbRule couchDbRule = new CouchDbRule();
    couchDbRule.before();
    couchDbRule.createServer();

    List<String> namesToDelete = new ArrayList<>();
    for ( String dbName : CouchUtils.listDatabases( couchDbRule.getCurrentServer() ) ) {
      if ( dbName.startsWith( couchDbRule.getTestDbBaseName() ) ) {
        namesToDelete.add( dbName );
      }
    }

    if ( namesToDelete.isEmpty() ) {
      System.out.println( "No test dbs found." );
      return;
    }

    System.out.println( "Will delete these databases @ " + couchDbRule.getServerUri() );
    for ( String dbNameToDelete : namesToDelete ) {
      System.out.println( "\t<" + dbNameToDelete + ">" );
    }

    System.out.println( "Press \"y\" to delete " + namesToDelete.size() + " databases." );
    int read = System.in.read();

    if ( read == 'y' ) {
      System.out.println( "Deleting..." );

      CouchServer server = couchDbRule.getCurrentServer();
      for ( String nameToDelete : namesToDelete ) {
        System.out.println( "\tDeleting <" + nameToDelete + ">" );
        server.deleteDatabase( nameToDelete );
      }

    } else {
      System.out.println( "Canceled." );
    }

    couchDbRule.after();
  }

}
