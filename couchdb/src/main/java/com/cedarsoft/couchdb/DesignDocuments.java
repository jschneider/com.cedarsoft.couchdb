package com.cedarsoft.couchdb;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DesignDocuments {
  @Nonnull
  public static final String JS_SUFFIX = "js";
  @Nonnull
  protected static final String REDUCE_SUFFIX = ".reduce." + JS_SUFFIX;
  @Nonnull
  protected static final String MAP_SUFFIX = ".map." + JS_SUFFIX;
  @Nonnull
  public static final String FILE_PROTOCOL = "file";

  @Nonnull
  public static File guessBaseDir( @Nonnull URL mapOrReduceScript ) throws FileNotFoundException {
    if ( !mapOrReduceScript.getProtocol( ).equals( FILE_PROTOCOL ) ) {
      throw new IllegalArgumentException( "Invalid protocol <" + mapOrReduceScript.getProtocol( ) + ">" );
    }

    File file = new File( mapOrReduceScript.getFile( ) );
    if ( !file.exists( ) ) {
      throw new FileNotFoundException( "File not found " + file.getAbsolutePath( ) );
    }

    File baseDir = file.getParentFile( );
    if ( !baseDir.isDirectory( ) ) {
      throw new FileNotFoundException( "Invalid base dir " + baseDir.getAbsolutePath( ) );
    }

    return baseDir;
  }

  @Nonnull
  public static Collection<? extends File> listJsFiles( @Nonnull File baseDir ) {
    return FileUtils.listFiles( baseDir, new String[]{JS_SUFFIX}, false );
  }

  @Nonnull
  public static DesignDocument createDesignDocument( @Nonnull String id, @Nonnull Iterable<? extends File> jsFiles ) throws IOException {
    DesignDocument designDocument = new DesignDocument( id );

    @Nonnull
    Map<String, String> mappingFunctions = new HashMap<String, String>( );
    @Nonnull
    Map<String, String> reduceFunctions = new HashMap<String, String>( );

    for ( File jsFile : jsFiles ) {
      String content = Files.toString( jsFile, Charsets.UTF_8 );
      if ( content.trim( ).isEmpty( ) ) {
        continue;
      }

      if ( isMappingFile( jsFile ) ) {
        String name = getBaseName( jsFile.getName( ) );
        mappingFunctions.put( name, content );
      } else if ( isReduceFile( jsFile ) ) {
        String name = getBaseName( jsFile.getName( ) );
        reduceFunctions.put( name, content );
      } else {
        throw new IllegalArgumentException( "Invalid file name <" + jsFile.getName( ) + ">" );
      }
    }

    //Now create the views
    for ( Map.Entry<String, String> entry : mappingFunctions.entrySet( ) ) {
      String name = entry.getKey( );
      String mappingFunction = entry.getValue( );

      @Nullable String reduceFunction = reduceFunctions.get( name );

      designDocument.add( new View( name, mappingFunction, reduceFunction ) );
    }

    if ( designDocument.hasViews( ) ) {

    }
    
    return designDocument;
  }

  @Nonnull
  private static String getBaseName( @Nonnull String fileName ) {
    int index = fileName.indexOf( "." );
    if ( index < 0 ) {
      throw new IllegalArgumentException( "invalid file name <" + fileName + ">" );
    }

    return fileName.substring( 0, index );
  }

  private static boolean isMappingFile( @Nonnull File jsFile ) {
    return jsFile.getName( ).endsWith( MAP_SUFFIX );
  }

  private static boolean isReduceFile( @Nonnull File jsFile ) {
    return jsFile.getName( ).endsWith( REDUCE_SUFFIX );
  }

  /**
   * Creates a design document for a given js file
   *
   * @param jsResource the javascript (reduce or mapping) file
   * @return the design document
   */
  @Nonnull
  public static DesignDocument createDesignDocument( @Nonnull URL jsResource ) throws IOException {
    File baseDir = guessBaseDir( jsResource );
    return createDesignDocument( baseDir );
  }

  @Nonnull
  public static DesignDocument createDesignDocument( @Nonnull File viewBaseDir ) throws IOException {
    Collection<? extends File> files = listJsFiles( viewBaseDir );
    return createDesignDocument( viewBaseDir.getName( ), files );
  }

  /**
   * Creates all design documents for the given js file
   *
   * @param jsResource the js resource
   * @return the created design documents
   */
  @Nonnull
  public static List<? extends DesignDocument> createDesignDocuments( @Nonnull URL jsResource ) throws IOException {
    File viewDir = guessBaseDir( jsResource );
    File viewsDir = viewDir.getParentFile( );

    return createDesignDocuments( viewsDir );
  }

  @Nonnull
  public static List<? extends DesignDocument> createDesignDocuments( @Nonnull File viewsDir ) throws IOException {
    List<DesignDocument> designDocuments = new ArrayList<DesignDocument>( );

    for ( File file : viewsDir.listFiles( ) ) {
      if ( !file.isDirectory( ) ) {
        continue;
      }

      designDocuments.add( createDesignDocument( file ) );
    }

    return designDocuments;
  }


  public static class View {
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
    public String getName( ) {
      return name;
    }

    @Nonnull
    public String getMappingFunction( ) {
      return mappingFunction;
    }

    @Nullable
    public String getReduceFunction( ) {
      return reduceFunction;
    }
  }

}
