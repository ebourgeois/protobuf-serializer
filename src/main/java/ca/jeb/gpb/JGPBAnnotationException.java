// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.gpb;

import ca.jeb.common.infra.JException;

/**
 * Exception when raised during the serialization of Protobuf attributes.
 * 
 * @see ca.jeb.common.gpb.ProtoBufSerializer
 */
public class JGPBAnnotationException extends JException
{
  /**
   * @param e - Exception
   */
  public JGPBAnnotationException(Exception e)
  {
    super(e);
  }

  /**
   * @param string
   */
  public JGPBAnnotationException(String string)
  {
    super(string);
  }
}