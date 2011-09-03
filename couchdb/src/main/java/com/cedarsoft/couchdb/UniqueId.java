package com.cedarsoft.couchdb;

import javax.annotation.Nonnull;

/**
 * a unique id describes the exact revision for a exact id
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class UniqueId {
  @Nonnull
  private final DocId id;
  @Nonnull
  private final Revision revision;

  public UniqueId( @Nonnull DocId id, @Nonnull Revision revision ) {
    this.id = id;
    this.revision = revision;
  }

  @Nonnull
  public DocId getId() {
    return id;
  }

  @Nonnull
  public Revision getRevision() {
    return revision;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( !( o instanceof UniqueId ) ) return false;

    UniqueId uniqueId = ( UniqueId ) o;

    if ( id != null ? !id.equals( uniqueId.id ) : uniqueId.id != null ) return false;
    if ( revision != null ? !revision.equals( uniqueId.revision ) : uniqueId.revision != null ) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + ( revision != null ? revision.hashCode() : 0 );
    return result;
  }

  @Deprecated
  @Nonnull
  public Revision getRev() {
    return getRevision();
  }
}
