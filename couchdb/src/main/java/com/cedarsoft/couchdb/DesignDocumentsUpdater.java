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

import com.cedarsoft.couchdb.core.ActionFailedException;
import com.cedarsoft.couchdb.core.DesignDocument;
import com.cedarsoft.couchdb.core.Revision;
import com.cedarsoft.couchdb.core.View;
import com.cedarsoft.couchdb.io.ActionResponseSerializer;
import com.cedarsoft.serialization.jackson.JacksonParserWrapper;
import com.cedarsoft.serialization.jackson.JacksonSupport;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Updates the design documents
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DesignDocumentsUpdater {
  @Nonnull
  private static final Logger LOG = Logger.getLogger( DesignDocumentsUpdater.class.getName() );
  @Nonnull
  private final CouchDatabase database;

  /**
   * Creates a new updater for the given database
   *
   * @param database the database
   */
  public DesignDocumentsUpdater( @Nonnull CouchDatabase database ) {
    this.database = database;
  }

  /**
   * Creates the json content for the design document
   *
   * @return a string containing the json content for this design document
   *
   * @throws IOException
   */
  public static String createJson( @Nonnull DesignDocument designDocument, @Nullable Revision revision ) throws IOException {
    //noinspection TypeMayBeWeakened
    StringWriter writer = new StringWriter();
    JsonGenerator generator = new JsonFactory().createJsonGenerator( writer );
    generator.writeStartObject();

    generator.writeStringField( "_id", designDocument.getId() );
    if ( revision != null ) {
      generator.writeStringField( "_rev", revision.asString() );
    }
    generator.writeStringField( "language", "javascript" );

    generator.writeObjectFieldStart( "views" );

    for ( View view : designDocument.getViews() ) {
      generator.writeObjectFieldStart( view.getName() );

      generator.writeStringField( "map", view.getMappingFunction() );
      @Nullable String reduceFunction = view.getReduceFunction();
      if ( reduceFunction != null ) {
        generator.writeStringField( "reduce", reduceFunction );
      }
      generator.writeEndObject();
    }

    generator.writeEndObject();
    generator.writeEndObject();
    generator.flush();
    return writer.toString();
  }

  /**
   * Uploads the given design documents to the database
   *
   * @param designDocuments the design documents
   * @throws IOException
   * @throws ActionFailedException
   */
  public void update( @Nonnull Iterable<? extends DesignDocument> designDocuments ) throws IOException, ActionFailedException {
    for ( DesignDocument designDocument : designDocuments ) {
      if ( !designDocument.hasViews() ) {
        continue;
      }

      if ( LOG.isLoggable( Level.INFO ) ) {
        LOG.info( "Updating document <" + designDocument.getId() + ">:" );
      }
      String path = designDocument.getDesignDocumentPath();
      WebResource resource = database.getDbRoot().path( path );


      @Nullable Revision currentRevision = getRevision( resource );

      if ( LOG.isLoggable( Level.FINE ) ) {
        LOG.fine( "PUT: " + resource.toString() );
      }
      ClientResponse response = resource.put( ClientResponse.class, createJson( designDocument, currentRevision ) );
      try {
        ActionResponseSerializer.verifyNoError( response );
      } finally {
        response.close();
      }
    }
  }

  /**
   * Returns the current revision (if there is one) or null
   *
   * @param path the path
   * @return the revision or null if there is no revision
   */
  @Nullable
  private static Revision getRevision( @Nonnull WebResource path ) throws ActionFailedException, IOException {
    if ( LOG.isLoggable( Level.FINE ) ) {
      LOG.fine( "HEAD: " + path.toString() );
    }
    ClientResponse response = path.get( ClientResponse.class );
    try {
      if ( LOG.isLoggable( Level.FINE ) ) {
        LOG.fine( "\tStatus: " + response.getStatus() );
      }
      if ( response.getClientResponseStatus() == ClientResponse.Status.NOT_FOUND ) {
        return null;
      }

      ActionResponseSerializer.verifyNoError( response );

      if ( response.getClientResponseStatus() != ClientResponse.Status.OK ) {
        throw new IllegalStateException( "Invalid response: " + response.getStatus() + ": " + response.getEntity( String.class ) );
      }

      JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
      try ( InputStream entityInputStream = response.getEntityInputStream() ) {
        JsonParser parser = jsonFactory.createJsonParser( entityInputStream );
        JacksonParserWrapper wrapper = new JacksonParserWrapper( parser );

        wrapper.nextToken( JsonToken.START_OBJECT );

        wrapper.nextFieldValue( "_id" );
        wrapper.nextFieldValue( "_rev" );
        return new Revision( wrapper.getText() );
      }
    } finally {
      response.close();
    }
  }
}
