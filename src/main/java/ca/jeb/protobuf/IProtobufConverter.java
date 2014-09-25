// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

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
  Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException;

  /**
   * @param sourceObject
   * @return Object
   * @throws CMSCoreAnnotationException
   */
  Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException;
}