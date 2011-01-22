package com.cedarsoft.couchdb.test;

import org.jcouchdb.exception.CouchDBException;
import org.junit.*;
import org.junit.runners.model.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class CouchDbRuleTest {
  public static final String COUCH_DB_RULE_TEST_SECOND = "couchdbruletest$second";
  public static final String COUCH_DB_RULE_TEST_THIRD = "couchdbruletest$third";
  private FrameworkMethod frameworkMethod;

  @Before
  public void setUp() throws Exception {
    frameworkMethod = new FrameworkMethod( getClass().getMethod( "testBasic" ) );
  }

  @Test
  public void testBasic() throws Throwable {
    final CouchDbRule rule = new CouchDbRule();
    assertRuleValuesAreNull( rule );

    final String[] dbName = {null};
    final boolean[] called = {false};
    rule.apply( new Statement() {
      @Override
      public void evaluate() throws Throwable {
        called[0] = true;
        assertThat( rule.getCurrentDb() ).isNotNull();
        assertThat( rule.getCurrentServer().isShutdown() ).isFalse();

        dbName[0] = rule.getCurrentDb().getDbName();
        assertThat( rule.getCurrentServer().listDatabases().contains( dbName[0] ) );

        assertThat( rule.createDb( COUCH_DB_RULE_TEST_SECOND ).getDbName() ).isEqualTo( COUCH_DB_RULE_TEST_SECOND );
        assertThat( rule.createDb( COUCH_DB_RULE_TEST_THIRD ).getDbName() ).isEqualTo( COUCH_DB_RULE_TEST_THIRD );
      }
    }, frameworkMethod, this ).evaluate();
    assertThat( called[0] ).isTrue();

    assertRuleValuesAreNull( rule );
    assertThat( rule.getCurrentServer().listDatabases().contains( dbName[0] ) ).isFalse();
    assertThat( rule.getCurrentServer().listDatabases().contains( COUCH_DB_RULE_TEST_SECOND ) ).isFalse();
    assertThat( rule.getCurrentServer().listDatabases().contains( COUCH_DB_RULE_TEST_THIRD ) ).isFalse();
  }

  @Test
  public void testWrongName() throws Throwable {
    final CouchDbRule rule = new CouchDbRule();

    rule.apply( new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          rule.createDb( "INVALID_NAme/" );
          fail( "Where is the Exception" );
        } catch ( CouchDBException ignore ) {
        }
      }
    }, frameworkMethod, this ).evaluate();
  }

  private void assertRuleValuesAreNull( CouchDbRule rule ) {
    try {
      rule.getCurrentDb();
      fail( "Where is the Exception" );
    } catch ( IllegalStateException ignore ) {
    }
  }
}
