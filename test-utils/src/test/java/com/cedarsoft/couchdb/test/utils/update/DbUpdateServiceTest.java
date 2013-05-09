package com.cedarsoft.couchdb.test.utils.update;

import com.cedarsoft.couchdb.test.utils.CouchTest;
import com.cedarsoft.couchdb.update.DbUpdateService;
import com.cedarsoft.exceptions.NotFoundException;
import com.cedarsoft.version.Version;
import org.junit.*;

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
      updateService.queryCurrentVersion();
      fail( "Where is the Exception" );
    } catch ( NotFoundException ignore ) {
    }

    updateService.setCurrentVersion( new Version( 1, 2, 3 ) );

    assertThat( updateService.queryCurrentVersion() ).isEqualTo( new Version( 1, 2, 3 ) );
  }
}