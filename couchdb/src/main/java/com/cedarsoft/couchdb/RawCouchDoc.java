package com.cedarsoft.couchdb;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A raw couch doc - without any further informations
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class RawCouchDoc {
  @NotNull
  @NonNls
  protected final DocId id;
  @Nullable
  @NonNls
  protected Revision rev;

  public RawCouchDoc( @NotNull @NonNls DocId id ) {
    this( id, null );
  }

  public RawCouchDoc( @NotNull @NonNls DocId id, @Nullable @NonNls Revision rev ) {
    this.id = id;
    this.rev = rev;
  }

  /**
   * Returns the id
   *
   * @return the id
   */
  @NotNull
  public DocId getId() {
    return id;
  }

  /**
   * Sets the revision. Should only be called when the doc has been updated
   *
   * @param rev the revision
   */
  void setRev( @Nullable Revision rev ) {
    this.rev = rev;
  }

  /**
   * Returns the revision
   *
   * @return the revision
   */
  @Nullable
  @NonNls
  public Revision getRev() {
    return rev;
  }
}
