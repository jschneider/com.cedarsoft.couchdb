package com.cedarsoft.couchdb;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchDbException extends Exception{
  public CouchDbException() {
  }

  public CouchDbException( String message ) {
    super( message );
  }

  public CouchDbException( String message, Throwable cause ) {
    super( message, cause );
  }

  public CouchDbException( Throwable cause ) {
    super( cause );
  }
}
