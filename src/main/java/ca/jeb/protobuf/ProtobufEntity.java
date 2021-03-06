// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.protobuf.GeneratedMessage;

/**
 * ProtoBufEntity annotation for defining your Protobuf entity
 * on your POJO class.
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProtobufEntity
{
  /**
   * This attribute should be used to indicate that this
   * interface should consider this field to be required
   * with ProtoBuf class.
   * 
   * @return Class&lt;? extends GeneratedMessage&gt;
   */
  Class<? extends GeneratedMessage> value();
}