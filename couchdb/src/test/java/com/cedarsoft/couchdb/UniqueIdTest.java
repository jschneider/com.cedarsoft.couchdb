package com.cedarsoft.couchdb;

import org.junit.*;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class UniqueIdTest {
  @Test
  public void testEquals() throws Exception {
    final DocId docId = new DocId( "asdf" );
    final Revision revision = new Revision( "asdfasdf" );
    assertThat( new UniqueId( docId, revision ) ).isEqualTo( new UniqueId( docId, revision ) );
  }

  @Test
  public void testEquals2() throws Exception {
    assertThat( new UniqueId( new DocId( "dadocid" ), new Revision( "darevision" ) ) ).isEqualTo( new UniqueId( new DocId( "dadocid" ), new Revision( "darevision" ) ) );
  }
}
