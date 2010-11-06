package com.cedarsoft.couchdb;

import com.cedarsoft.Version;
import com.cedarsoft.VersionException;
import com.cedarsoft.VersionRange;
import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
 */
public class Bar {
  private int value;
  private String description;

  public Bar( int value, String description ) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public void setValue( int value ) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }
  
  public static class Serializer extends AbstractJacksonSerializer<Bar> {
    public Serializer() {
      super( "bar", VersionRange.single( 1, 0, 0 ) );
    }

    @Override
    public void serialize( @NotNull JsonGenerator serializeTo, @NotNull Bar object, @NotNull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
      serializeTo.writeNumberField( "value", object.getValue() );
      serializeTo.writeStringField( "description", object.getDescription() );
    }

    @NotNull
    @Override
    public Bar deserialize( @NotNull JsonParser deserializeFrom, @NotNull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
      nextFieldValue( deserializeFrom, "value" );
      int value = deserializeFrom.getIntValue();

      nextFieldValue( deserializeFrom, "description" );
      String description = deserializeFrom.getText();

      closeObject( deserializeFrom );
      return new Bar( value, description );
    }
  }
  
}
