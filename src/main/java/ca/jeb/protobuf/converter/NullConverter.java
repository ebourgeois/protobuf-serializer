// Copyright (c) 2014 Morgan Stanley & Co. Incorporated, All Rights Reserved

package ca.jeb.protobuf.converter;

import ca.jeb.protobuf.IProtobufConverter;
import ca.jeb.protobuf.ProtobufAnnotationException;

/**
 * This is a class to convert Strings to Date objects.
 */
public class NullConverter implements IProtobufConverter
{
  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtoBufConverter#convertToProtoBuf(java.lang.Object)
   */
  @Override
  public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException
  {
    return sourceObject;
  }

  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtoBufConverter#convertFromProtoBuf(java.lang.Object)
   */
  @Override
  public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException
  {
    return sourceObject;
  }
}