package com.cedarsoft.couchdb.io;

import java.io.ByteArrayOutputStream;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class MaxLengthByteArrayOutputStream extends ByteArrayOutputStream {
  public final int maxLength;

  public MaxLengthByteArrayOutputStream() {
    this( 8192 );
  }

  public MaxLengthByteArrayOutputStream( int maxLength ) {
    this.maxLength = maxLength;
  }

  @Override
  public synchronized void write( int b ) {
    if ( count > maxLength ) {
      return;
    }
    super.write( b );
  }


  @Override
  public synchronized void write( byte[] b, int off, int len ) {
    if ( count > maxLength ) {
      return;
    }
    super.write( b, off, len );
  }
}
