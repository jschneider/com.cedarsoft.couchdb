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

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Describes a design document
 *
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class DesignDocument {
  @Nonnull
  private final String id;
  @Nonnull
  private final Collection<DesignDocuments.View> views = new ArrayList<DesignDocuments.View>();

  public DesignDocument( @Nonnull String id ) {
    this.id = id;
  }

  public void add( @Nonnull DesignDocuments.View view ) {
    this.views.add( view );
  }

  @Nonnull
  public Collection<? extends DesignDocuments.View> getViews() {
    return Collections.unmodifiableCollection( views );
  }

  @Nonnull
  public String getId() {
    return id;
  }

  /**
   * Creates the json content for the design document
   *
   * @return a string containing the json content for this design document
   *
   * @throws IOException
   */
  @Nonnull
  public String createJson() throws IOException {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = new JsonFactory().createJsonGenerator( writer );
    generator.writeStartObject();

    generator.writeStringField( "_id", id );
    generator.writeStringField( "language", "javascript" );

    generator.writeObjectFieldStart( "views" );

    for ( DesignDocuments.View view : views ) {
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

  public boolean hasViews() {
    return !views.isEmpty();
  }

  @Nonnull
  public String getDesignDocumentPath() {
    return "_design/" + getId();
  }
}
