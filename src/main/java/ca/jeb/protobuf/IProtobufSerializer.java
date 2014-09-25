// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import com.google.protobuf.Message;

/**
 * Interface for generating Protobuf serialization; generally only the
 * ProtobufSerializer should implement this, but by all means, feel free
 * to create your own!
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public interface IProtobufSerializer
{
  /**
   * Implement the toProtobuf method that will convert this
   * object to a GeneratedMessage G.
   * 
   * @param pojo - POJO object to serialize into a Protobuf of generic type G
   * @return Message - a new instance of a Protobuf GeneratedMessage, which implements Message
   * @throws ProtobufException
   */
  Message toProtobuf(Object pojo) throws ProtobufException;

  /**
   * Implement the fromProtobuf method to set all ProtoBufAttributes
   * on the supplied POJO class.
   * 
   * @param protoBuf - GeneratedMessage, the object to "deserialize" into a POJO
   * @param pojoClazz - Class type to "deserialize" to
   * @return Object - a new instance of <i>pojoClazz</i>
   * @throws ProtobufException
   */
  Object fromProtobuf(Message protoBuf, Class<? extends Object> pojoClazz) throws ProtobufException;
}