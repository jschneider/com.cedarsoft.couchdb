package com.cedarsoft.couchdb.test.utils.update;

import com.cedarsoft.couchdb.DesignDocument;
import com.cedarsoft.couchdb.test.utils.CouchTest;
import com.cedarsoft.couchdb.update.DbUpdateService;
import com.cedarsoft.couchdb.update.DesignDocumentsVersionInfo;
import com.cedarsoft.exceptions.NotFoundException;
import com.cedarsoft.version.Version;
import com.google.common.collect.ImmutableList;
import org.junit.*;

import javax.annotation.Nonnull;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DbUpdateServiceTest extends CouchTest {
  private DbUpdateService updateService;

  @Before
  public void setUp() throws Exception {
    updateService = new DbUpdateService( db() );
  }

  @Test
  public void testVersion() throws Exception {
    try {
      updateService.queryCurrentVersionInfo();
      fail( "Where is the Exception" );
    } catch ( NotFoundException ignore ) {
    }

    DesignDocumentsVersionInfo versionInfo = new DesignDocumentsVersionInfo( new Version( 1, 2, 3 ), 123123123L, "asdf" );
    updateService.setCurrentVersion( versionInfo, null );

    assertThat( updateService.queryCurrentVersionInfo() ).isEqualTo( versionInfo );
  }

  @Ignore
  @Test
  public void testString() throws Exception {
    String descriptionString = DbUpdateService.createDescriptionString();
    assertThat( descriptionString ).isEqualTo( "johannes@moria64" );
  }

  /**
   * @noinspection ConstantConditions
   */
  @Test
  public void testUpdate() throws Exception {
    final DesignDocument designDocument = new DesignDocument( "daId" );

    DbUpdateService.DesignDocumentsProvider provider = new DbUpdateService.DesignDocumentsProvider() {
      @Nonnull
      @Override
      public Version getVersion() {
        return Version.valueOf( 1, 2, 3 );
      }

      @Nonnull
      @Override
      public Iterable<? extends DesignDocument> getDesignDocuments() {
        return ImmutableList.of( designDocument );
      }
    };

    DesignDocumentsVersionInfo result = updateService.updateIfNecessary( provider );
    assertThat( result ).isNotNull();
    assertThat( result.getVersion() ).isEqualTo( provider.getVersion() );

    assertThat( updateService.updateIfNecessary( provider ) ).isNull();


    //New Version

    assertThat( updateService.updateIfNecessary( new DbUpdateService.DesignDocumentsProvider() {
      @Nonnull
      @Override
      public Version getVersion() {
        return Version.valueOf( 2, 0, 0 );
      }

      @Nonnull
      @Override
      public Iterable<? extends DesignDocument> getDesignDocuments() {
        return ImmutableList.of( designDocument );
      }
    } ).getVersion() ).isEqualTo( Version.valueOf( 2, 0, 0 ) );
  }
}