package com.cedarsoft.couchdb;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class Revision {
  @NotNull
  @NonNls
  private final String rev;

  public Revision( @NonNls @NotNull String rev ) {
    this.rev = rev;
  }

  @NotNull
  @NonNls
  public String getRev() {
    return rev;
  }

  @NotNull
  @NonNls
  public String asString() {
    return rev;
  }

  @Override
  public String toString() {
    return rev;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( o == null || getClass() != o.getClass() ) return false;

    Revision revision = ( Revision ) o;

    if ( !rev.equals( revision.rev ) ) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return rev.hashCode();
  }
}
