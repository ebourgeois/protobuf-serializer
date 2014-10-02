// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import ca.jeb.common.infra.JException;

/**
 * Exception when raised during the serialization of Protobuf attributes.
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public class ProtobufException extends JException
{
  /**
   * @param exception - {@link Exception}
   */
  public ProtobufException(Exception exception)
  {
    super(exception);
  }

  /**
   * @param string - Exception string
   */
  public ProtobufException(String string)
  {
    super(string);
  }

  /**
   * @param string - Exception string
   * @param exception - {@link Exception}
   */
  public ProtobufException(String string, Exception exception)
  {
    super(string, exception);
  }
}