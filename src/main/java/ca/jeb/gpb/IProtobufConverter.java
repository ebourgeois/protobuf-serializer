// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.gpb;

/**
 * An interface class that any classes attribute needs to be
 * converted for a Google ProtoBuf Message.
 */
public interface IProtobufConverter
{
  /**
   * @param sourceObject
   * @return Object
   * @throws CMSCoreAnnotationException
   */
  Object convertToProtoBuf(Object sourceObject) throws JGPBAnnotationException;

  /**
   * @param sourceObject
   * @return Object
   * @throws CMSCoreAnnotationException
   */
  Object convertFromProtoBuf(Object sourceObject) throws JGPBAnnotationException;
}