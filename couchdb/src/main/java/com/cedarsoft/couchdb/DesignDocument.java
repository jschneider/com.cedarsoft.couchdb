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
* @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
*/
public class DesignDocument {
  @Nonnull
  private final String id;
  @Nonnull
  private final Collection<DesignDocuments.View> views = new ArrayList<DesignDocuments.View>( );

  public DesignDocument( @Nonnull String id ) {
    this.id = id;
  }

  public void add( @Nonnull DesignDocuments.View view ) {
    this.views.add( view );
  }

  @Nonnull
  public Collection<? extends DesignDocuments.View> getViews( ) {
    return Collections.unmodifiableCollection( views );
  }

  @Nonnull
  public String getId( ) {
    return id;
  }

  @Nonnull
  public String createJson( ) throws IOException {
    StringWriter writer = new StringWriter( );
    JsonGenerator generator = new JsonFactory( ).createJsonGenerator( writer );
    generator.writeStartObject();

    generator.writeStringField( "_id", id );
    generator.writeStringField( "language", "javascript" );

    generator.writeObjectFieldStart( "views" );

    for ( DesignDocuments.View view : views ) {
      generator.writeObjectFieldStart( view.getName( ) );

      generator.writeStringField( "map", view.getMappingFunction( ) );
      @Nullable String reduceFunction = view.getReduceFunction( );
      if ( reduceFunction != null ) {
        generator.writeStringField( "reduce", reduceFunction );
      }
      generator.writeEndObject( );
    }


    generator.writeEndObject( );
    generator.writeEndObject( );
    generator.flush();
    return writer.toString( );
  }

  public boolean hasViews( ) {
    return !views.isEmpty( );
  }

  @Nonnull
  public String getDesignDocumentPath( ) {
    return "_design/" + getId( );
  }
}
