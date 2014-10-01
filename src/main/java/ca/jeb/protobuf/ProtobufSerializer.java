// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.jeb.common.infra.JException;
import ca.jeb.common.infra.JReflectionUtils;
import ca.jeb.common.infra.JStringUtils;
import ca.jeb.protobuf.converter.NullConverter;
import ca.jeb.protobuf.internal.ProtobufSerializerUtils;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public class ProtobufSerializer implements IProtobufSerializer
{
  private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufSerializer.class);

  /**
   * @see ca.jeb.protobuf.IProtobufSerializer#toProtobuf(java.lang.Object)
   */
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Message toProtobuf(Object pojo) throws ProtobufException
  {
    try
    {
      final Class<?> fromClazz = (Class<?>)pojo.getClass();
      final Class<? extends GeneratedMessage> protoClazz = ProtobufSerializerUtils.getProtobufClassFromPojoAnno(fromClazz);
      if (protoClazz == null)
      {
        throw new ProtobufAnnotationException("Doesn't seem like " + fromClazz + " is ProtobufEntity");
      }

      final Map<Field, ProtobufAttribute> protoBufFields = ProtobufSerializerUtils.getAllProtbufFields(fromClazz);
      if (protoBufFields.isEmpty())
      {
        return null;
      }

      final Method newBuilderMethod = protoClazz.getMethod("newBuilder");
      final Builder protoObjBuilder = (Builder)newBuilderMethod.invoke(null);
      for (Entry<Field, ProtobufAttribute> entry : protoBufFields.entrySet())
      {
        final Field field = entry.getKey();
        final ProtobufAttribute gpbAnnotation = entry.getValue();
        final String fieldName = field.getName();

        // 1. Determine validity of value
        Object value = getPojoFieldValue(pojo, gpbAnnotation, field);
        // If value is null and it is not required, skip, as the default for Protobuf values is null
        if (value == null)
        {
          continue;
        }

        // 2. Call recursively if this is a ProtobufEntity
        value = serializeToProtobufEntity(value);

        // 3. Handle POJO Collections/Lists
        if (value instanceof Collection)
        {
          value = convertCollectionToProtobufs((Collection<Object>)value);
          if (((Collection)value).isEmpty())
          {
            continue;
          }
        }

        // 4. Determine the setter name
        final String setter = ProtobufSerializerUtils.getProtobufSetter(gpbAnnotation, field, value);

        // 5. Finally, set the value on the Builder
        setProtobufFieldValue(gpbAnnotation, protoObjBuilder, setter, value);
      }

      return protoObjBuilder.build();

    }
    catch (Exception e)
    {
      throw new ProtobufException("Could not generate Protobuf object for " + pojo.getClass() + ": " + e, e);
    }
  }

  /**
   * @see ca.jeb.common.gpb.IProtobufGenerator#fromProtobuf(com.google.protobuf.GeneratedMessage)
   */
  @Override
  public Object fromProtobuf(Message protobuf, Class<?> pojoClazz) throws ProtobufException
  {
    try
    {
      final Class<? extends GeneratedMessage> protoClazz = ProtobufSerializerUtils.getProtobufClassFromPojoAnno(pojoClazz);
      if (protoClazz == null)
      {
        throw new ProtobufAnnotationException("Doesn't seem like " + pojoClazz + " is ProtobufEntity");
      }

      final Map<Field, ProtobufAttribute> protobufFields = ProtobufSerializerUtils.getAllProtbufFields(pojoClazz);
      if (protobufFields.isEmpty())
      {
        throw new ProtobufException("No protoBuf fields have been annotated on the class " + pojoClazz + ", thus cannot continue.");
      }

      Object pojo = pojoClazz.newInstance();

      for (Entry<Field, ProtobufAttribute> entry : protobufFields.entrySet())
      {
        final Field field = entry.getKey();
        final ProtobufAttribute protobufAttribute = entry.getValue();
        final String setter = ProtobufSerializerUtils.getPojoSetter(protobufAttribute, field);

        Object protobufValue = getProtobufFieldValue(protobuf, protobufAttribute, field);
        if (protobufValue == null)
        {
          continue;
        }

        setPojoFieldValue(pojo, setter, protobufValue, protobufAttribute);
      }

      return pojo;
    }
    catch (Exception e)
    {
      throw new ProtobufException("Could not generate POJO of type " + pojoClazz + " from Protobuf object " + protobuf.getClass() + ": "
              + e, e);
    }
  }

  /**
   * Returns the field value from the supplied <code>pojo</code> object. If a <code>pojoGetter</code has been set in the
   * {@link ProtobufAttribute}, then use that, otherwise, try getting the field value directly.
   * 
   * @param pojo - POJO Object
   * @param protobufAttribute
   * @param field
   * @return Object - the resulting object after calling the POJO's getter
   * @throws ProtobufAnnotationException
   */
  private static final Object getPojoFieldValue(Object pojo, ProtobufAttribute protobufAttribute, Field field)
          throws ProtobufAnnotationException
  {
    final String getter = protobufAttribute.pojoGetter();

    Object value = null;
    if (!getter.isEmpty())
    {
      try
      {
        return JReflectionUtils.runMethod(pojo, getter);
      }
      catch (Exception e)
      {
        throw new ProtobufAnnotationException("Could not get a value for field " + field.getName() + " using configured getter of "
                + getter, e);
      }
    }

    try
    {
      value = JReflectionUtils.runGetter(pojo, field);
    }
    catch (Exception ee)
    {
      throw new ProtobufAnnotationException("Could not execute getter " + getter + " on class " + pojo.getClass().getCanonicalName() + ": "
              + ee, ee);
    }

    if (value == null && protobufAttribute.required())
    {
      throw new ProtobufAnnotationException("Required field " + field.getName() + " on class " + pojo.getClass().getCanonicalName()
              + " is null");
    }

    return value;
  }

  @SuppressWarnings("rawtypes")
  private static final Object getProtobufFieldValue(Message protoBuf, ProtobufAttribute protobufAttribute, Field field) throws JException,
          InstantiationException, IllegalAccessException
  {
    final String fieldName = field.getName();
    final String upperCaseFirstFieldName = JStringUtils.upperCaseFirst(fieldName);
    final String getter = ProtobufSerializerUtils.getProtobufGetter(protobufAttribute, field);
    // This is used to determine if the Protobuf message has populated this value

    Boolean isCollection = Boolean.FALSE;
    if (Collection.class.isAssignableFrom(field.getType()))
    {
      isCollection = Boolean.TRUE;
    }
    if (!isCollection)
    {
      // no need to continue if this field in the protoBuf is not set
      final String haser = StringUtils.replaceOnce(getter, "get", "has");

      final boolean fieldIsSet = (boolean)JReflectionUtils.runMethod(protoBuf, haser, (Object[])null);
      if (!fieldIsSet)
      {
        return null;
      }
    }

    // Go ahead and fun the getter
    Object protobufValue = JReflectionUtils.runMethod(protoBuf, getter, (Object[])null);
    if (isCollection && ((Collection)protobufValue).isEmpty())
    {
      return null;
    }

    // If the field itself is a ProtbufEntity, serialize that!
    if (protobufValue instanceof GeneratedMessage && ProtobufSerializerUtils.isProtbufEntity(field.getType()))
    {
      protobufValue = serializeFromProtobufEntity((Message)protobufValue, field.getType());
    }

    if (protobufValue instanceof Collection)
    {
      protobufValue = convertCollectionFromProtobufs(field, (Collection<?>)protobufValue);
      if (((Collection)protobufValue).isEmpty())
      {
        return null;
      }
    }

    return protobufValue;
  }

  /**
   * Create a new serializer and serializes the supplied object/attribute.
   * 
   * @param pojo - the POJO object
   * @return Object - the resulting GPB object
   * @throws JException
   */
  private static final Object serializeToProtobufEntity(Object pojo) throws JException
  {
    final ProtobufEntity protoBufEntity = ProtobufSerializerUtils.getProtobufEntity(pojo.getClass());

    if (protoBufEntity == null)
    {
      return pojo;
    }

    return new ProtobufSerializer().toProtobuf(pojo);
  }

  /**
   * Create a new serializer and (de)serializes the supplied Protobu/attribute to a POJO of type <i>pojoClazz</i>.
   * 
   * @param protoBuf
   * @param pojoClazz
   * @return
   * @throws JException
   */
  private static final Object serializeFromProtobufEntity(Message protoBuf, Class<?> pojoClazz) throws JException
  {
    final ProtobufEntity protoBufEntity = ProtobufSerializerUtils.getProtobufEntity(pojoClazz);

    if (protoBufEntity == null)
    {
      return protoBuf;
    }

    return new ProtobufSerializer().fromProtobuf(protoBuf, pojoClazz);
  }

  /**
   * Loops through the collection of objects and serializes them, iff they have ProtobufEntity annotations.
   * 
   * @param value - Collection<Object>
   * @return Object - Collection of serialized objects.
   * @throws JException
   */
  private static final Object convertCollectionToProtobufs(Collection<Object> collectionOfNonProtobufs) throws JException
  {
    if (collectionOfNonProtobufs.isEmpty())
    {
      return collectionOfNonProtobufs;
    }
    final Object first = collectionOfNonProtobufs.toArray()[0];
    if (!ProtobufSerializerUtils.isProtbufEntity(first))
    {
      return collectionOfNonProtobufs;
    }

    final Collection<Object> newCollectionValues;

    /**
     * Maintain the Collection type of value at this stage (if it is a Set), and if conversion is required to a
     * different Collection type, that will be handled by a converter later on
     */
    if (collectionOfNonProtobufs instanceof Set)
    {
      newCollectionValues = new HashSet<>();
    }
    else
    {
      newCollectionValues = new ArrayList<>();
    }

    for (Object iProtobufGenObj : collectionOfNonProtobufs)
    {
      newCollectionValues.add(serializeToProtobufEntity(iProtobufGenObj));
    }

    return newCollectionValues;
  }

  private static Object convertCollectionFromProtobufs(Field field, Collection<?> collectionOfProtobufs) throws JException,
          InstantiationException, IllegalAccessException
  {
    if (collectionOfProtobufs.isEmpty())
    {
      return collectionOfProtobufs;
    }

    final ParameterizedType listType = (ParameterizedType)field.getGenericType();
    final Class<?> collectionClazzType = (Class<?>)listType.getActualTypeArguments()[0];
    final ProtobufEntity protoBufEntityAnno = ProtobufSerializerUtils.getProtobufEntity(collectionClazzType);

    final Object first = collectionOfProtobufs.toArray()[0];
    if (!(first instanceof GeneratedMessage) && protoBufEntityAnno == null)
    {
      return collectionOfProtobufs;
    }

    final Collection<Object> newCollectionOfValues = new ArrayList<>();
    for (Object protobufValue : collectionOfProtobufs)
    {
      if (!(protobufValue instanceof GeneratedMessage))
      {
        throw new ProtobufException("Collection contains an object of type " + protobufValue.getClass()
                + " which is not an instanceof GeneratedMessage, can not (de)serialize this");
      }
      newCollectionOfValues.add(serializeFromProtobufEntity((Message)protobufValue, collectionClazzType));
    }

    return newCollectionOfValues;
  }

  /**
   * This method does the actual "set" on the Protobuf builder. If the user specified a converter,
   * then use that right before we actually try and set the value.
   * 
   * @param protobufAttribute - ProtobufAttribute
   * @param protoObjBuilder - G.Builder
   * @param setter - String
   * @param fieldValue - POJO's filed Object
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws ProtobufAnnotationException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   */
  private static final void setProtobufFieldValue(ProtobufAttribute protobufAttribute, Builder protoObjBuilder, String setter,
          Object fieldValue) throws NoSuchMethodException, SecurityException, ProtobufAnnotationException, InstantiationException,
          IllegalAccessException, IllegalArgumentException, InvocationTargetException
  {
    Class<? extends Object> fieldValueClass = fieldValue.getClass();
    Class<? extends Object> gpbClass = fieldValueClass;

    final Class<? extends IProtobufConverter> converterClazz = protobufAttribute.converter();
    if (converterClazz != NullConverter.class)
    {
      final IProtobufConverter protoBufConverter = (IProtobufConverter)converterClazz.newInstance();
      fieldValue = protoBufConverter.convertToProtobuf(fieldValue);
      gpbClass = fieldValue.getClass();
      fieldValueClass = gpbClass;
    }

    // Need to convert the argument class from non-primitives to primitives, as Protobuf uses these.
    gpbClass = ProtobufSerializerUtils.getProtobufClass(fieldValue, gpbClass);

    final Method gpbMethod = protoObjBuilder.getClass().getDeclaredMethod(setter, gpbClass);
    gpbMethod.invoke(protoObjBuilder, fieldValue);
  }

  /**
   * This method does the actual "set" on the POJO instance. If the user specified a converter,
   * then use that right before we actually try and set the value.
   * 
   * @param pojo - the instance of the POJO
   * @param setter - the setter method name
   * @param protobufValue - the Protobuf value, note this could be anything from another ProtobufEntity to a primitive, like boolean
   * @param protobufAttribute - the ProtobufAttribute annotation
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws JException
   */
  private static final void setPojoFieldValue(Object pojo, String setter, Object protobufValue, ProtobufAttribute protobufAttribute)
          throws InstantiationException, IllegalAccessException, JException
  {
    /**
     * convertCollectionFromProtoBufs() above returns an ArrayList, and we may have a converter to convert to a Set,
     * so we are performing the conversion there
     */
    final Class<? extends IProtobufConverter> fromProtoBufConverter = protobufAttribute.converter();
    if (fromProtoBufConverter != NullConverter.class)
    {
      final IProtobufConverter converter = fromProtoBufConverter.newInstance();
      protobufValue = converter.convertFromProtobuf(protobufValue);
    }

    Class<? extends Object> argClazz = protobufValue.getClass();

    JReflectionUtils.runSetter(pojo, setter, protobufValue, argClazz);
  }
}