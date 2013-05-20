package com.cedarsoft.couchdb.test.utils.foo;

import com.cedarsoft.couchdb.core.ViewDescriptor;

import javax.annotation.Nonnull;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public interface Views {
  interface Doc1 {
    String DESIGN_DOCUMENT_ID = "doc1";

    @Nonnull
    ViewDescriptor A_VIEW = new ViewDescriptor( DESIGN_DOCUMENT_ID, "aView" );//$NON-NLS-1$
  }


}
