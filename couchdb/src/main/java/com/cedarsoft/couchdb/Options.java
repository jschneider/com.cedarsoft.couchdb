package com.cedarsoft.couchdb;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Query options
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class Options {
  public static final String KEY = "key";
  public static final String STARTKEY = "startkey";
  public static final String STARTKEY_DOCID = "startkey_docid";
  public static final String ENDKEY = "endkey";
  public static final String ENDKEY_DOCID = "endkey_docid";
  public static final String LIMIT = "limit";
  public static final String UPDATE = "update";
  public static final String DESCENDING = "descending";
  public static final String SKIP = "skip";
  public static final String GROUP = "group";
  public static final String STALE = "stale";
  public static final String REDUCE = "reduce";
  public static final String INCLUDE_DOCS = "include_docs";
  public static final String GROUP_LEVEL = "group_level";

  private final Map<String, String> content = new HashMap<String, String>();

  @Nonnull
  public static Options empty() {
    return new Options();
  }

  public Options() {
  }

  public Options( @Nullable Options options ) {
    if ( options != null ) {
      this.content.putAll( options.getParams() );
    }
  }

  @Nonnull
  protected Options put( @Nonnull String key, @Nonnull String value ) {
    content.put( key, value );
    return this;
  }

  @Nonnull
  public Options key( @Nonnull Key key ) {
    return put( KEY, key.getJson() );
  }

  @Nonnull
  public Options startKey( @Nonnull Key key ) {
    return put( STARTKEY, key.getJson() );
  }

  @Nonnull
  public Options startKeyDocId( @Nonnull String docId ) {
    return put( STARTKEY_DOCID, docId );
  }

  @Nonnull
  public Options endKey( @Nonnull Key key ) {
    return put( ENDKEY, key.getJson() );
  }

  @Nonnull
  public Options endKeyDocId( @Nonnull String docId ) {
    return put( ENDKEY_DOCID, docId );
  }

  @Nonnull
  public Options limit( int limit ) {
    return put( LIMIT, String.valueOf( limit ) );
  }

  @Nonnull
  public Options update( boolean update ) {
    return put( UPDATE, String.valueOf( update ) );
  }

  @Nonnull
  public Options descending( boolean update ) {
    return put( DESCENDING, String.valueOf( update ) );
  }

  @Nonnull
  public Options skip( int skip ) {
    return put( SKIP, String.valueOf( skip ) );
  }

  @Nonnull
  public Options group( boolean group ) {
    return put( GROUP, String.valueOf( group ) );
  }

  @Nonnull
  public Options stale() {
    return put( STALE, "ok" );
  }

  @Nonnull
  public Options reduce( boolean reduce ) {
    return put( REDUCE, String.valueOf( reduce ) );
  }

  @Nonnull
  public Options includeDocs( boolean includeDocs ) {
    return put( INCLUDE_DOCS, String.valueOf( includeDocs ) );
  }

  public boolean isIncludeDocs() {
    return Boolean.TRUE.toString().equals( get( INCLUDE_DOCS ) );
  }

  @Nonnull
  public Options groupLevel( int level ) {
    return put( GROUP_LEVEL, String.valueOf( level ) );
  }


  @Nonnull
  public Map<String, String> getParams() {
    return Collections.unmodifiableMap( content );
  }

  @Nonnull
  public String toQuery() {
    if ( content.isEmpty() ) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    builder.append( "?" );

    boolean first = true;
    for ( Map.Entry<String, String> entry : content.entrySet() ) {
      if ( !first ) {
        builder.append( "&" );
      }

      builder.append( entry.getKey() ).append( "=" );
      try {
        builder.append( URLEncoder.encode( entry.getValue(), "UTF-8" ) );
      } catch ( UnsupportedEncodingException e ) {
        throw new RuntimeException( e );
      }
      first = false;
    }

    return builder.toString();
  }

  @Nullable
  public String get( @Nonnull String key ) {
    return content.get( key );
  }

  @Nonnull
  public Set<String> keys() {
    return content.keySet();
  }

  @Override
  public String toString() {
    return toQuery();
  }
}
