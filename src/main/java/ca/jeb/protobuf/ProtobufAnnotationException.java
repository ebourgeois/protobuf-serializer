// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

/**
 * Exception when raised during the serialization of Protobuf attributes.
 * 
 * @see ca.jeb.protobuf.ProtobufSerializer
 */
public class ProtobufAnnotationException extends ProtobufException
{
  /**
   * @param exception - {@link Exception}
   */
  public ProtobufAnnotationException(Exception exception)
  {
    super(exception);
  }

  /**
   * @param string - Exception string
   */
  public ProtobufAnnotationException(String string)
  {
    super(string);
  }

  /**
   * @param string - Exception string
   * @param exception - {@link Exception}
   */
  public ProtobufAnnotationException(String string, Exception exception)
  {
    super(string, exception);
  }
}