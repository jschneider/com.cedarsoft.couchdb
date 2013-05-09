package com.cedarsoft.couchdb.update;

import com.cedarsoft.version.Version;

import javax.annotation.Nonnull;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DesignDocumentsVersionInfo {
  @Nonnull
  private final Version version;
  /**
   * The date of the last update
   */
  private final long updatedAt;

  /**
   * A string describing the updater
   */
  @Nonnull
  private final String updatedBy;

  public DesignDocumentsVersionInfo( @Nonnull Version version, long updatedAt, @Nonnull String updatedBy ) {
    this.version = version;
    this.updatedAt = updatedAt;
    this.updatedBy = updatedBy;
  }

  @Nonnull
  public Version getVersion() {
    return version;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  @Nonnull
  public String getUpdatedBy() {
    return updatedBy;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof DesignDocumentsVersionInfo ) ) {
      return false;
    }

    DesignDocumentsVersionInfo that = ( DesignDocumentsVersionInfo ) obj;

    if ( updatedAt != that.updatedAt ) {
      return false;
    }
    if ( !updatedBy.equals( that.updatedBy ) ) {
      return false;
    }
    if ( !version.equals( that.version ) ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = version.hashCode();
    result = 31 * result + ( int ) ( updatedAt ^ updatedAt >>> 32 );
    result = 31 * result + updatedBy.hashCode();
    return result;
  }
}
