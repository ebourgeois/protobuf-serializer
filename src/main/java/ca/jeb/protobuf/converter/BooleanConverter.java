// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf.converter;

import ca.jeb.protobuf.IProtobufConverter;
import ca.jeb.protobuf.ProtobufAnnotationException;

/**
 * Converts BigDecimal values to double.
 */
public class BooleanConverter implements IProtobufConverter
{

  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtobufConverter#convert(java.lang.Object)
   */
  @Override
  public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException
  {
    final Boolean b = (Boolean)sourceObject;

    return b;
  }

  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtobufConverter#convertFromProtobuf(java.lang.Object)
   */
  @Override
  public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException
  {
    final boolean bl = (boolean)sourceObject;

    return new Boolean(bl);
  }
}