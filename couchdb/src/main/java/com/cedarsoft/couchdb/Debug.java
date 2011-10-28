package com.cedarsoft.couchdb;

import java.io.PrintStream;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class Debug {
  private static boolean assertEnabled;

  static {
    assert assertEnabled = true;
  }

  public static boolean isEnabled() {
    return assertEnabled;
  }

  public static PrintStream out() {
    return System.out;
  }
}
