/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *         http://www.cedarsoft.org/gpl3ce
 *         (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
 */
package com.cedarsoft.couchdb;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Offers support for DesignDocuments
 *
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

  /**
   * Guesses a base dir for a given file(!) url.
   * This method can be used to determine the base dir that can be used to find several design documents that are uploaded to the db.
   *
   * @param mapOrReduceScript a map or reduce script that is used to determine the base dir
   * @return the guessed base dir
   *
   * @throws FileNotFoundException
   */
  @Nonnull
  public static File guessBaseDir( @Nonnull URL mapOrReduceScript ) throws FileNotFoundException {
    if ( !mapOrReduceScript.getProtocol().equals( FILE_PROTOCOL ) ) {
      throw new IllegalArgumentException( "Invalid protocol <" + mapOrReduceScript.getProtocol() + ">" );
    }

    File file = new File( mapOrReduceScript.getFile() );
    if ( !file.exists() ) {
      throw new FileNotFoundException( "File not found " + file.getAbsolutePath() );
    }

    File baseDir = file.getParentFile();
    if ( !baseDir.isDirectory() ) {
      throw new FileNotFoundException( "Invalid base dir " + baseDir.getAbsolutePath() );
    }

    return baseDir;
  }

  /**
   * Lists all js files within the given directory
   *
   * @param baseDir the base dir
   * @return the list of all files ending with "js"
   */
  @Nonnull
  public static Collection<? extends File> listJsFiles( @Nonnull File baseDir ) {
    return ImmutableList.copyOf( baseDir.listFiles( new PatternFilenameFilter( ".*" + JS_SUFFIX ) ) );
  }

  /**
   * Creates a new design document
   * @param baseClass the base class
   * @param viewDescriptors the view descriptors (must have the same design document id )
   * @return the design document
   */
  @Nonnull
  public static DesignDocument createDesignDocument( @Nonnull Class<?> baseClass, @Nonnull ViewDescriptor... viewDescriptors ) throws IOException {
    return createDesignDocument( baseClass, ImmutableList.copyOf( viewDescriptors ) );
  }

  public static DesignDocument createDesignDocument( @Nonnull Class<?> baseClass, @Nonnull List<? extends ViewDescriptor> viewDescriptors ) throws IOException {
    if ( viewDescriptors.isEmpty() ) {
      throw new IllegalArgumentException( "Need at least one view descriptor" );
    }

    String designDocumentId = viewDescriptors.get( 0 ).getDesignDocumentId();

    @Nonnull
    Map<String, String> mappingFunctions = new HashMap<String, String>();
    @Nonnull
    Map<String, String> reduceFunctions = new HashMap<String, String>();

    for ( ViewDescriptor viewDescriptor : viewDescriptors ) {
      //the mapping file
      {
        String path = createMapPath( viewDescriptor );
        @Nullable URL url = baseClass.getResource( path );
        if ( url == null ) {
          throw new IllegalStateException( "No mapping file found for <" + viewDescriptor + "> @ <" + path + "> (" + baseClass.getName() + ")" );
        }

        String content = new String( ByteStreams.toByteArray( url.openStream() ) );
        mappingFunctions.put( viewDescriptor.getViewId(), content );
      }

      //the reduce file
      {
        String path = createReducePath( viewDescriptor );
        @Nullable URL url = baseClass.getResource( path );
        if ( url == null ) {
          continue;
        }

        String content = new String( ByteStreams.toByteArray( url.openStream() ) );
        reduceFunctions.put( viewDescriptor.getViewId(), content );
      }
    }

    return bundle( designDocumentId, mappingFunctions, reduceFunctions );
  }

  @Nonnull
  private static String createMapPath( @Nonnull ViewDescriptor viewDescriptor ) {
    return viewDescriptor.getDesignDocumentId() + "/" + viewDescriptor.getViewId() + MAP_SUFFIX;
  }

  @Nonnull
  private static String createReducePath( @Nonnull ViewDescriptor viewDescriptor ) {
    return viewDescriptor.getDesignDocumentId() + "/" + viewDescriptor.getViewId() + REDUCE_SUFFIX;
  }

  /**
   * Creates a design document for the given js files (view and map functions)
   *
   * @param id      the id
   * @param jsFiles the js files (the view and map functions)
   * @return the design document
   *
   * @throws IOException
   */
  @Nonnull
  public static DesignDocument createDesignDocument( @Nonnull String id, @Nonnull Iterable<? extends File> jsFiles ) throws IOException {
    @Nonnull
    Map<String, String> mappingFunctions = new HashMap<String, String>();
    @Nonnull
    Map<String, String> reduceFunctions = new HashMap<String, String>();

    for ( File jsFile : jsFiles ) {
      String content = Files.toString( jsFile, Charsets.UTF_8 );
      if ( content.trim().isEmpty() ) {
        continue;
      }

      if ( isMappingFile( jsFile.getName() ) ) {
        String name = getBaseName( jsFile.getName() );
        mappingFunctions.put( name, content );
      } else if ( isReduceFile( jsFile.getName() ) ) {
        String name = getBaseName( jsFile.getName() );
        reduceFunctions.put( name, content );
      } else {
        throw new IllegalArgumentException( "Invalid file name <" + jsFile.getName() + ">" );
      }
    }

    return bundle( id, mappingFunctions, reduceFunctions );
  }

  /**
   * Bundles a design document
   * @param id the id
   * @param mappingFunctions the mapping functions (key is name, value is content)
   * @param reduceFunctions the reduce functions (key is name, value is content)
   * @return the design document
   */
  @Nonnull
  private static DesignDocument bundle( @Nonnull String id, @Nonnull Map<String, String> mappingFunctions, @Nonnull Map<String, String> reduceFunctions ) {
    //Now create the views
    DesignDocument designDocument = new DesignDocument( id );

    for ( Map.Entry<String, String> entry : mappingFunctions.entrySet() ) {
      String name = entry.getKey();
      String mappingFunction = entry.getValue();

      @Nullable String reduceFunction = reduceFunctions.get( name );

      designDocument.add( new View( name, mappingFunction, reduceFunction ) );
    }

    return designDocument;
  }

  /**
   * Returns the base name
   *
   * @param fileName the base name
   * @return
   */
  @Nonnull
  private static String getBaseName( @Nonnull String fileName ) {
    int index = fileName.indexOf( "." );
    if ( index < 0 ) {
      throw new IllegalArgumentException( "invalid file name <" + fileName + ">" );
    }

    return fileName.substring( 0, index );
  }

  private static boolean isMappingFile( @Nonnull String fileName ) {
    return fileName.endsWith( MAP_SUFFIX );
  }

  private static boolean isReduceFile( @Nonnull String fileName ) {
    return fileName.endsWith( REDUCE_SUFFIX );
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
    return createDesignDocument( viewBaseDir.getName(), files );
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
    File viewsDir = viewDir.getParentFile();

    return createDesignDocuments( viewsDir );
  }

  @Nonnull
  public static List<? extends DesignDocument> createDesignDocuments( @Nonnull File viewsDir ) throws IOException {
    List<DesignDocument> designDocuments = new ArrayList<DesignDocument>();

    for ( File file : viewsDir.listFiles() ) {
      if ( !file.isDirectory() ) {
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

}
