package com.cedarsoft.couchdb.test.utils;

import com.cedarsoft.couchdb.DesignDocument;
import com.cedarsoft.couchdb.DesignDocuments;
import com.cedarsoft.couchdb.DesignDocumentsProvider;
import com.cedarsoft.version.Version;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Attention: The resource must be a file!
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class FileBasedDesignDocumentsProvider implements DesignDocumentsProvider {
  @Nonnull
  public static final String FILE_PROTOCOL = "file";

  @Nonnull
  private final URL resource;
  @Nonnull
  private final Version version;

  /**
   * Attention: The resource must be a file!
   *
   * @param resource the resource (must be a file!)
   * @param version the version
   */
  public FileBasedDesignDocumentsProvider( @Nonnull URL resource, @Nonnull Version version ) {
    this.resource = resource;
    this.version = version;

    if ( !resource.getProtocol().equals( FILE_PROTOCOL ) ) {
      throw new IllegalArgumentException( "Invalid protocol <" + resource.getProtocol() + "> for resource <" + resource + ">" );
    }
  }

  @Override
  @Nonnull
  public List<? extends DesignDocument> getDesignDocuments() throws IOException {
    return DesignDocuments.createDesignDocuments( resource );
  }

  @Nonnull
  @Override
  public Version getVersion() {
    return version;
  }
}
