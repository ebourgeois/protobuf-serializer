// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ca.jeb.common.infra.JStringUtils;
import ca.jeb.protobuf.converter.NullConverter;

/**
 * ProtobufAttribute annotation for defining your POJO attributes
 * for serialization to and from Protobuf classes/entities.
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ProtobufAttribute
{
  /**
   * This attribute should be used to indicate that this
   * interface should consider this field to be required
   * with ProtoBuf class.
   * 
   * @return boolean
   */
  boolean required() default false;

  /**
   * This attribute defines what the target method should be
   * used on the {@link ProtobufEntity}. This will default to
   * "set" + upperCaseFirst(fieldName).
   * 
   * @return String
   */
  String protobufSetter() default JStringUtils.EMPTY;

  /**
   * This attribute defines what the getter method name
   * should be to retrieve the data <b>from</b> the {@link ProtobufEntity} object.
   * This defaults to "get" + upperCaseFirst(fieldName).
   * 
   * @return String
   */
  String protobufGetter() default JStringUtils.EMPTY;

  /**
   * This attribute defines what the getter method name
   * should be to retrieve the data <b>from</b> the POJO object.
   * This defaults to "get" + upperCaseFirst(fieldName).
   * 
   * @return String
   */
  String pojoGetter() default JStringUtils.EMPTY;

  /**
   * This attribute defines what the target setter method
   * should be used on the POJO object. This will default to
   * "set" + upperCaseFirst(fieldName).
   * 
   * @return String
   */
  String pojoSetter() default JStringUtils.EMPTY;

  /**
   * This should be set to a class that implements the IProtoBufConverter.
   * This converter will be used to convert the value from the
   * POJO objects to the Google ProtoBuf object; default is {@link NullConverter}.
   * 
   * @return Class&lt;? extends IProtobufConverter&gt;
   */
  Class<? extends IProtobufConverter> converter() default NullConverter.class;
}