package com.cedarsoft.couchdb;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DocId {
  @NotNull
  @NonNls
  private final String id;

  public DocId( @NotNull @NonNls String id ) {
    this.id = id;
  }

  @NotNull
  @NonNls
  public String asString() {
    return id;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( o == null || getClass() != o.getClass() ) return false;

    DocId docId = ( DocId ) o;

    if ( !id.equals( docId.id ) ) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return id;
  }
}
