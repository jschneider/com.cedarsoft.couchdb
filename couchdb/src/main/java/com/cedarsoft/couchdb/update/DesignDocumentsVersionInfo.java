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
}
