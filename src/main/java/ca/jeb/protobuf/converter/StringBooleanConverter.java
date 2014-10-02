// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf.converter;

import ca.jeb.protobuf.IProtobufConverter;
import ca.jeb.protobuf.ProtobufAnnotationException;

/**
 * Converts String values to boolean going to Protobuf, and vice-versa from Protobuf.
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public class StringBooleanConverter implements IProtobufConverter
{

  /**
   * @see ca.jeb.protobuf.IProtobufConverter#convertToProtobuf(java.lang.Object)
   */
  @Override
  public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException
  {
    final String bl = (String)sourceObject;

    return new Boolean(bl).booleanValue();
  }

  /**
   * @see ca.jeb.protobuf.IProtobufConverter#convertFromProtobuf(java.lang.Object)
   */
  @Override
  public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException
  {
    final boolean b = (boolean)sourceObject;

    return new Boolean(b).toString();
  }
}