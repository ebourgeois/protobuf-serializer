// Copyright (c) 2014 Morgan Stanley & Co. Incorporated, All Rights Reserved

package ca.jeb.gpb.converter;

import ca.jeb.gpb.IProtobufConverter;
import ca.jeb.gpb.JGPBAnnotationException;

/**
 * This is a class to convert Strings to Date objects.
 */
public class NullConverter implements IProtobufConverter
{
  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtoBufConverter#convertToProtoBuf(java.lang.Object)
   */
  @Override
  public Object convertToProtoBuf(Object sourceObject) throws JGPBAnnotationException
  {
    return sourceObject;
  }

  /**
   * @see com.ms.corptsy.cmscore.infra.annotations.IProtoBufConverter#convertFromProtoBuf(java.lang.Object)
   */
  @Override
  public Object convertFromProtoBuf(Object sourceObject) throws JGPBAnnotationException
  {
    return sourceObject;
  }
}