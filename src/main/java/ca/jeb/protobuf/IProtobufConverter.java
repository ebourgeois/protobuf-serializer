// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

/**
 * An interface class that any classes attribute needs to be
 * converted for a Google ProtoBuf Message.
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public interface IProtobufConverter
{
  /**
   * @param sourceObject - Object to convert from
   * @return Object - The converted object
   * @throws ProtobufAnnotationException
   */
  Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException;

  /**
   * @param sourceObject - Object to convert from
   * @return Object - The converted object
   * @throws ProtobufAnnotationException
   */
  Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException;
}