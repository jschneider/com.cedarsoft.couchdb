package com.cedarsoft.couchdb;

import javax.annotation.Nonnull;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class ViewDescriptor {
  @Nonnull
  private final String designDocumentId;

  @Nonnull
  private final String viewId;

  public ViewDescriptor( @Nonnull String designDocumentId, @Nonnull String viewId ) {
    this.designDocumentId = designDocumentId;
    this.viewId = viewId;
  }

  @Nonnull
  public String getDesignDocumentId() {
    return designDocumentId;
  }

  @Nonnull
  public String getViewId() {
    return viewId;
  }
}
