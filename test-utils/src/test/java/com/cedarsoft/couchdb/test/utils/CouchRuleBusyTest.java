package com.cedarsoft.couchdb.test.utils;

import org.junit.*;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchRuleBusyTest {
  @Test
  public void testIt() throws Exception {
    for ( int i = 0; i < 100; i++ ) {
      CouchDbRule rule = new CouchDbRule();
      rule.before();
      rule.after();
    }
  }
}
