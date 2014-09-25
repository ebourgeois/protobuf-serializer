// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf.converter;

import java.math.BigDecimal;

import ca.jeb.protobuf.IProtobufConverter;
import ca.jeb.protobuf.ProtobufAnnotationException;

/**
 * Converts BigDecimal values to String and vice-versa.
 */
public class BigDecimalStringConverter implements IProtobufConverter
{

  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtoBufConverter#convert(java.lang.Object)
   */
  @Override
  public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException
  {
    final BigDecimal bd = (BigDecimal)sourceObject;

    return bd.toPlainString();
  }

  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtoBufConverter#convertFromProto(java.lang.Object)
   */
  @Override
  public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException
  {
    return new BigDecimal((String)sourceObject);
  }
}