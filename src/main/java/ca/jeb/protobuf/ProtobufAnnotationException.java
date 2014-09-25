// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;


/**
 * Exception when raised during the serialization of Protobuf attributes.
 * 
 * @see ca.jeb.common.gpb.ProtoBufSerializer
 */
public class ProtobufAnnotationException extends ProtobufException
{
  /**
   * @param e - Exception
   */
  public ProtobufAnnotationException(Exception e)
  {
    super(e);
  }

  /**
   * @param string
   */
  public ProtobufAnnotationException(String string)
  {
    super(string);
  }

  /**
   * @param string
   * @param e
   */
  public ProtobufAnnotationException(String string, Exception e)
  {
    super(string, e);
  }
}