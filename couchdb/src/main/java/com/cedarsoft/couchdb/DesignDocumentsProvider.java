package com.cedarsoft.couchdb;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Provides the design documents
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public interface DesignDocumentsProvider {

  /**
   * Returns the design documents that are uploaded.
   *
   * @return the design documents
   *
   * @throws IOException
   */
  @Nonnull
  List<? extends DesignDocument> getDesignDocuments() throws IOException;
}