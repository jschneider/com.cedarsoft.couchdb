package com.cedarsoft.couchdb.core;

import javax.annotation.Nullable;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public interface HasRawData {
  /**
   * Returns the raw data if there is any
   * @return the raw data
   */
  @Nullable
  byte[] getRaw();
}