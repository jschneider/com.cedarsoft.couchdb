package com.cedarsoft.couchdb;

import javax.annotation.Nonnull;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class Key {
  @Nonnull
  private final String json;

  public Key( @Nonnull String json ) {
    this.json = json;
  }

  @Nonnull
  public String getJson() {
    return json;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( !( o instanceof Key ) ) return false;

    Key key = ( Key ) o;

    if ( json != null ? !json.equals( key.json ) : key.json != null ) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return json != null ? json.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Key{" +
      json + '\'' +
      '}';
  }
}
