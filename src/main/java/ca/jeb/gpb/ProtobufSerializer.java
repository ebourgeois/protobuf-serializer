// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.gpb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.jeb.common.gpb.ProtoBufSerializer;
import ca.jeb.common.infra.JException;
import ca.jeb.common.infra.JReflectionUtils;
import ca.jeb.common.infra.JStringUtils;
import ca.jeb.gpb.converter.NullConverter;

import com.google.protobuf.GeneratedMessage;

/**
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public class ProtobufSerializer<G extends GeneratedMessage, P extends Object> implements IProtobufSerializer<G, P>
{
  private static final Map<Class<? extends Object>, Class<? extends Object>> GPB_JAVA_MAPPING         = new ConcurrentHashMap<>();
  private static final Map<String, Map<Field, ProtobufAttribute>>            CLASS_TO_FIELD_MAP_CACHE = new ConcurrentHashMap<>();

  private static final Logger                                                LOGGER                   = LoggerFactory
                                                                                                              .getLogger(ProtoBufSerializer.class);

  /**
   * @see ca.jeb.common.gpb.IProtoBufGenerator#toProtoBuf(java.lang.Object)
   */
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public G toProtoBuf(P pojo) throws JException
  {
    try
    {
      final Class<P> fromClazz = (Class<P>)pojo.getClass();
      final Class<G> protoClazz = getProtobufClassFromPojoAnno(fromClazz);
      if (protoClazz == null)
      {
        throw new JGPBAnnotationException("Can not serialize a " + fromClazz + " without the ProtobufEntity annotation on " + fromClazz);
      }

      final Map<Field, ProtobufAttribute> protoBufFields = getAllProtbufFields(fromClazz);
      if (protoBufFields.isEmpty())
      {
        return null;
      }

      final Method newBuilderMethod = protoClazz.getMethod("newBuilder");
      final G.Builder protoObjBuilder = (G.Builder)newBuilderMethod.invoke(null);
      for (Entry<Field, ProtobufAttribute> entry : protoBufFields.entrySet())
      {
        final Field field = entry.getKey();
        final ProtobufAttribute gpbAnnotation = entry.getValue();
        final String fieldName = field.getName();

        // 1. Determine validity of value
        Object value = JReflectionUtils.runGetter(pojo, field);

        if (value == null && gpbAnnotation.required())
        {
          throw new JGPBAnnotationException("Required field '" + fieldName + "' is null");
        }

        // If value is null and it is not required, skip, as the default for Protobuf values is null
        if (value == null)
        {
          continue;
        }

        // 2. Call recursively if this is a ProtobufEntity
        value = serializeProtobufEntity(value);

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
        final String setter = getSetter(gpbAnnotation, fieldName, value);

        // 5. Finally, set the value on the Builder
        setFieldValue(gpbAnnotation, protoObjBuilder, setter, value);
      }

      return (G)protoObjBuilder.build();

    }
    catch (Exception e)
    {
      throw new JException("Could not generate ProtoBuf object for " + this.getClass() + ": " + e, e);
    }
  }

  /**
   * @see ca.jeb.common.gpb.IProtoBufGenerator#fromProtoBuf(com.google.protobuf.GeneratedMessage)
   */
  @Override
  public P fromProtoBuf(G protoBuf) throws JException
  {
    return null;
  }

  /**
   * Return a ProtobufEntity annotation from any object sent, null if there is none.
   * 
   * @param object - any object
   * @return ProtobufEntity annotation
   * @see ProtobufEntity
   */
  private static final ProtobufEntity getProtobufEntity(Class<?> clazz)
  {
    final ProtobufEntity protoBufEntity = clazz.getAnnotation(ProtobufEntity.class);

    if (protoBufEntity != null)
    {
      return protoBufEntity;
    }

    return null;
  }

  /**
   * Return the Protobuf Class based on the pojo class, i.e. grab the value from the ProtobufEntity annotation.
   * 
   * @param clazz
   * @return Class
   */
  @SuppressWarnings("unchecked")
  private Class<G> getProtobufClassFromPojoAnno(Class<P> clazz)
  {
    final ProtobufEntity annotation = getProtobufEntity(clazz);
    final Class<G> gpbClazz = (Class<G>)annotation.value();
    if (gpbClazz == null)
    {
      return null;
    }
    return gpbClazz;
  }

  /**
   * Returns a full mapping of all Protobuf fields from the POJO class.
   * Essentially, the only fields that will be returned if they have
   * the ProtoBufAttribute annotation.
   * 
   * @param fromClazz
   * @return Map<Field, ProtoBufAttribute>
   */
  private static final Map<Field, ProtobufAttribute> getAllProtbufFields(Class<? extends Object> fromClazz)
  {
    Map<Field, ProtobufAttribute> protoBufFields = CLASS_TO_FIELD_MAP_CACHE.get(fromClazz.getCanonicalName());
    if (protoBufFields != null)
    {
      return protoBufFields;
    }

    protoBufFields = new HashMap<>();
    final List<Field> fields = JReflectionUtils.getAllFields(new ArrayList<Field>(), fromClazz);

    for (Field field : fields)
    {
      final Annotation annotation = field.getAnnotation(ProtobufAttribute.class);
      if (annotation == null)
      {
        continue;
      }
      final ProtobufAttribute gpbAnnotation = (ProtobufAttribute)annotation;
      protoBufFields.put(field, gpbAnnotation);
    }

    // Caching to increase speed
    CLASS_TO_FIELD_MAP_CACHE.put(fromClazz.getCanonicalName(), protoBufFields);

    return protoBufFields;
  }

  /**
   * Returns true if there is a ProtobufEntity annotation on this object.
   * 
   * @param object - a POJO object
   * @return boolean - true, if there is a ProtobufEntity annotation, else, false
   * @see ProtobufEntity
   */
  private static final boolean isProtbufEntity(Object object)
  {
    final ProtobufEntity protoBufEntity = getProtobufEntity(object.getClass());

    if (protoBufEntity != null)
    {
      return true;
    }
    return false;
  }

  /**
   * Create a new serializer and serializes the supplied object/attribute.
   * 
   * @param pojo - the POJO object
   * @return Object - the resulting GPB object
   * @throws JException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static final Object serializeProtobufEntity(Object pojo) throws JException
  {
    final ProtobufEntity protoBufEntity = getProtobufEntity(pojo.getClass());

    if (protoBufEntity == null)
    {
      return pojo;
    }

    final ProtobufSerializer serializer = new ProtobufSerializer<>();

    return serializer.toProtoBuf(pojo);
  }

  /**
   * Loops through the collection of objects and serializes them, iff they have ProtobufEntity annotations.
   * 
   * @param value - Collection<Object>
   * @return Object - Collection of serialized objects.
   * @throws JException
   */
  private static final Object convertCollectionToProtobufs(Collection<Object> value) throws JException
  {
    if (value.isEmpty())
    {
      return value;
    }
    final Object first = value.toArray()[0];
    if (!isProtbufEntity(first))
    {
      return value;
    }

    final Collection<Object> newCollectionValues;

    /**
     * Maintain the Collection type of value at this stage (if it is a Set), and if conversion is required to a
     * different Collection type, that will be handled by a converter later on
     */
    if (value instanceof Set)
    {
      newCollectionValues = new HashSet<>();
    }
    else
    {
      newCollectionValues = new ArrayList<>();
    }

    for (Object iProtoBufGenObj : value)
    {
      newCollectionValues.add(serializeProtobufEntity(iProtoBufGenObj));
    }

    return newCollectionValues;
  }

  /**
   * Retrieve the ProtobufClass based on the POJO value. The returned value may get converted,
   * as the Protobuf builders/setters use primitives. For example, if user has declared <code>Integer</code>,
   * this get's converted to <code>int</code>.
   * 
   * @param value - POJO object
   * @param gpbClass -
   * @return Class<? extends Object> - actual Protobuf class to use in setter
   */
  private static final Class<? extends Object> getProtobufClass(Object value, Class<? extends Object> protobufClass)
  {
    if (value instanceof Integer)
    {
      return Integer.TYPE;
    }
    if (value instanceof Boolean)
    {
      return Boolean.TYPE;
    }
    if (value instanceof Double)
    {
      return Double.TYPE;
    }
    if (value instanceof Long || value instanceof Date)
    {
      return Long.TYPE;
    }
    if (value instanceof List)
    {
      return Iterable.class;
    }
    return protobufClass;
  }

  /**
   * Return the setter for the Protobuf builder.
   * <ol>
   * <li>Defaults to just "set" + upper case the first character of the fieldName.
   * <li>If it's a collection, use the "addAll" type method Protobuf has
   * <li>Otherwise, use the override value from the user's ProtobufAttribute annotation
   * <ol>
   * 
   * @param protobufAttribute
   * @param fieldName - String
   * @param fieldValue - POJO's filed Object
   * @return String - the setter to be used on Protobuf builder
   */
  private static final String getSetter(ProtobufAttribute protobufAttribute, String fieldName, Object fieldValue)
  {
    String setter = "set" + JStringUtils.upperCaseFirst(fieldName);

    if (fieldValue instanceof Collection)
    {
      setter = "addAll" + JStringUtils.upperCaseFirst(fieldName);
    }

    // Finally override setter with a value that is configured in ProtobufAttribute annotation
    final String configedSetter = protobufAttribute.protoBufSetter();
    if (!configedSetter.equals(JStringUtils.EMPTY))
    {
      setter = configedSetter;
    }

    return setter;
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
   * @throws JGPBAnnotationException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   */
  @SuppressWarnings("rawtypes")
  private final void setFieldValue(ProtobufAttribute protobufAttribute, G.Builder protoObjBuilder, String setter, Object fieldValue)
          throws NoSuchMethodException, SecurityException, JGPBAnnotationException, InstantiationException, IllegalAccessException,
          IllegalArgumentException, InvocationTargetException
  {
    Class<? extends Object> fieldValueClass = fieldValue.getClass();
    Class<? extends Object> gpbClass = fieldValueClass;

    final Class<? extends IProtobufConverter> converterClazz = protobufAttribute.converter();
    if (converterClazz != NullConverter.class)
    {
      final IProtobufConverter protoBufConverter = (IProtobufConverter)converterClazz.newInstance();
      fieldValue = protoBufConverter.convertToProtoBuf(fieldValue);
      gpbClass = fieldValue.getClass();
      fieldValueClass = gpbClass;
    }

    // Need to convert the argument class from non-primitives to primitives, as ProtoBuf uses these.
    gpbClass = getProtobufClass(fieldValue, gpbClass);

    final Method gpbMethod = protoObjBuilder.getClass().getDeclaredMethod(setter, gpbClass);
    gpbMethod.invoke(protoObjBuilder, fieldValue);
  }
}