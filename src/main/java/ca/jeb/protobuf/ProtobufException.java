// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import ca.jeb.common.infra.JException;

/**
 * Exception when raised during the serialization of Protobuf attributes.
 * 
 * @see ca.jeb.common.gpb.ProtoBufSerializer
 */
public class ProtobufException extends JException
{
  /**
   * @param e - Exception
   */
  public ProtobufException(Exception e)
  {
    super(e);
  }

  /**
   * @param string
   */
  public ProtobufException(String string)
  {
    super(string);
  }

  /**
   * @param string
   * @param e
   */
  public ProtobufException(String string, Exception e)
  {
    super(string, e);
  }
}