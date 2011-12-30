package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.DesignDocument;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Provides the design documents
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public interface DesignDocumentsProvider {
  @Nonnull
  List<? extends DesignDocument> getDesignDocuments();
}