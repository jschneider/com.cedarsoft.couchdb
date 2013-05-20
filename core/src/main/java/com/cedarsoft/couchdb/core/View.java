package com.cedarsoft.couchdb.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a view
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class View {
  @Nonnull
  private final String name;
  @Nonnull
  private final String mappingFunction;
  @Nullable
  private final String reduceFunction;

  public View( @Nonnull String name, @Nonnull String mappingFunction, @Nullable String reduceFunction ) {
    this.name = name;
    this.mappingFunction = mappingFunction;
    this.reduceFunction = reduceFunction;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getMappingFunction() {
    return mappingFunction;
  }

  @Nullable
  public String getReduceFunction() {
    return reduceFunction;
  }
}
