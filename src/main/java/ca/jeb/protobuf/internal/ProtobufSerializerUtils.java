// Copyright (c) 2014 Morgan Stanley & Co. Incorporated, All Rights Reserved

package ca.jeb.protobuf.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import ca.jeb.common.infra.JReflectionUtils;
import ca.jeb.common.infra.JStringUtils;
import ca.jeb.protobuf.ProtobufAttribute;
import ca.jeb.protobuf.ProtobufEntity;

import com.google.protobuf.GeneratedMessage;

/**
 * Utility class to help out the ProtobufSerializer.
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public final class ProtobufSerializerUtils
{
  private static final Map<String, Map<Field, ProtobufAttribute>> CLASS_TO_FIELD_MAP_CACHE         = Collections
                                                                                                           .synchronizedMap(new WeakHashMap<String, Map<Field, ProtobufAttribute>>());

  // Internal cache to hold onto Class -> fieldName -> setter
  private static final Map<String, Map<String, String>>           CLASS_TO_FIELD_SETTERS_MAP_CACHE = Collections
                                                                                                           .synchronizedMap(new WeakHashMap<String, Map<String, String>>());

  // Internal cache to hold onto Class -> fieldName -> getter
  private static final Map<String, Map<String, String>>           CLASS_TO_FIELD_GETTERS_MAP_CACHE = Collections
                                                                                                           .synchronizedMap(new WeakHashMap<String, Map<String, String>>());

  /**
   * Retrieve the ProtobufClass based on the POJO value. The returned value may get converted,
   * as the Protobuf builders/setters use primitives. For example, if user has declared <code>Integer</code>,
   * this get's converted to <code>int</code>.
   * 
   * @param value - POJO object
   * @param protobufClass - the Protobuf class
   * @return Class&lt;? extends Object&gt; - actual Protobuf class to use in setter
   */
  public static final Class<? extends Object> getProtobufClass(Object value, Class<? extends Object> protobufClass)
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
   * Return a ProtobufEntity annotation from any object sent, null if there is none.
   * 
   * @param clazz - any Class
   * @return ProtobufEntity annotation
   * @see ca.jeb.protobuf.ProtobufEntity
   */
  public static final ProtobufEntity getProtobufEntity(Class<?> clazz)
  {
    final ProtobufEntity protoBufEntity = clazz.getAnnotation(ProtobufEntity.class);

    if (protoBufEntity != null)
    {
      return protoBufEntity;
    }

    return null;
  }

  /**
   * Returns true if there is a ProtobufEntity annotation on this object.
   * 
   * @param object - a POJO object
   * @return boolean - true, if there is a ProtobufEntity annotation, else, false
   * @see ProtobufEntity
   */
  public static final boolean isProtbufEntity(Object object)
  {
    return isProtbufEntity(object.getClass());
  }

  /**
   * Returns true if there is a ProtobufEntity annotation on this class.
   * 
   * @param clazz - any Class
   * @return boolean - true, if there is a ProtobufEntity annotation, else, false
   * @see ProtobufEntity
   */
  public static final boolean isProtbufEntity(Class<?> clazz)
  {
    final ProtobufEntity protoBufEntity = getProtobufEntity(clazz);

    if (protoBufEntity != null)
    {
      return true;
    }
    return false;
  }

  /**
   * Return the Protobuf Class based on the pojo class, i.e. grab the value from the ProtobufEntity annotation.
   * 
   * @param clazz - any Class
   * @return Class&lt;? extends GeneratedMessage&gt;
   */
  public static final Class<? extends GeneratedMessage> getProtobufClassFromPojoAnno(Class<?> clazz)
  {
    final ProtobufEntity annotation = getProtobufEntity(clazz);
    final Class<? extends GeneratedMessage> gpbClazz = (Class<? extends GeneratedMessage>)annotation.value();
    if (gpbClazz == null)
    {
      return null;
    }
    return gpbClazz;
  }

  /**
   * Returns a full mapping of all Protobuf fields from the POJO class.
   * Essentially, the only fields that will be returned if they have
   * the ProtobufAttribute annotation.
   * 
   * @param fromClazz - Class&lt;? extends Object&gt;
   * @return Map&lt;Field, ProtobufAttribute&gt;
   */
  public static final Map<Field, ProtobufAttribute> getAllProtbufFields(Class<? extends Object> fromClazz)
  {
    Map<Field, ProtobufAttribute> protoBufFields = CLASS_TO_FIELD_MAP_CACHE.get(fromClazz.getCanonicalName());
    if (protoBufFields != null)
    {
      return protoBufFields;
    }
    else
    {
      protoBufFields = new HashMap<>();
    }

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

  /*
   * Setter and Getter Utilities
   */

  /**
   * Return the setter for the Protobuf builder. <br>
   * 1. Defaults to just "set" + upper case the first character of the fieldName.
   * 2. If it's a collection, use the "addAll" type method Protobuf has
   * 3. Otherwise, use the override value from the user's ProtobufAttribute annotation <br>
   * 
   * @param protobufAttribute - {@link ProtobufAttribute}
   * @param field - String
   * @param fieldValue - POJO's filed Object
   * @return String - the setter to be used on Protobuf builder
   */
  public static final String getProtobufSetter(ProtobufAttribute protobufAttribute, Field field, Object fieldValue)
  {
    final String fieldName = field.getName();
    final String upperClassName = field.getDeclaringClass().getCanonicalName();
    // Look at the cache first
    Map<String, String> map = CLASS_TO_FIELD_SETTERS_MAP_CACHE.get(upperClassName);
    if (map != null)
    {
      if (!map.isEmpty() && map.containsKey(fieldName))
      {
        return map.get(fieldName);
      }
    }
    else
    {
      map = new ConcurrentHashMap<>();
    }

    String setter = "set" + JStringUtils.upperCaseFirst(fieldName);

    if (fieldValue instanceof Collection)
    {
      setter = "addAll" + JStringUtils.upperCaseFirst(fieldName);
    }

    // Finally override setter with a value that is configured in ProtobufAttribute annotation
    final String configedSetter = protobufAttribute.protobufSetter();
    if (!configedSetter.equals(JStringUtils.EMPTY))
    {
      setter = configedSetter;
    }

    map.put(fieldName, setter);
    CLASS_TO_FIELD_SETTERS_MAP_CACHE.put(upperClassName, map);

    return setter;
  }

  /**
   * Retrieve the getter against the Protobuf class; default is to is "get" plus upper case first character of the field name.
   * 
   * @param protobufAttribute - {@link ProtobufAttribute}
   * @param field - {@link Field}
   * @return String
   */
  public static final String getProtobufGetter(ProtobufAttribute protobufAttribute, Field field)
  {
    final String fieldName = field.getName();
    final String upperClassName = field.getDeclaringClass().getCanonicalName();
    // Look at the cache first
    Map<String, String> map = CLASS_TO_FIELD_GETTERS_MAP_CACHE.get(upperClassName);
    if (map != null)
    {
      if (!map.isEmpty() && map.containsKey(fieldName))
      {
        return map.get(fieldName);
      }
    }
    else
    {
      map = new ConcurrentHashMap<>();
    }

    final String upperCaseFirstFieldName = JStringUtils.upperCaseFirst(field.getName());
    String getter = "get" + upperCaseFirstFieldName;

    if (Collection.class.isAssignableFrom(field.getType()))
    {
      getter += "List";
    }
    if (!protobufAttribute.protobufGetter().isEmpty())
    {
      return protobufAttribute.protobufGetter();
    }

    map.put(fieldName, getter);
    CLASS_TO_FIELD_GETTERS_MAP_CACHE.put(upperClassName, map);

    return getter;
  }

  /**
   * Retrieve the setter on the POJO class; default is to is "set" plus upper case first character of the field name.
   * 
   * @param protobufAttribute - {@link ProtobufAttribute}
   * @param field - {@link Field}
   * @return String - the name of the POJO setter
   */
  public static final String getPojoSetter(ProtobufAttribute protobufAttribute, Field field)
  {
    final String fieldName = field.getName();
    final String upperClassName = field.getDeclaringClass().getCanonicalName();
    // Look at the cache first
    Map<String, String> map = CLASS_TO_FIELD_SETTERS_MAP_CACHE.get(upperClassName);
    if (map != null)
    {
      if (!map.isEmpty() && map.containsKey(fieldName))
      {
        return map.get(fieldName);
      }
    }
    else
    {
      map = new ConcurrentHashMap<>();
    }

    final String upperCaseFirstFieldName = JStringUtils.upperCaseFirst(field.getName());
    String setter = "set" + upperCaseFirstFieldName;

    if (!protobufAttribute.pojoSetter().isEmpty())
    {
      return protobufAttribute.pojoSetter();
    }

    map.put(fieldName, setter);
    CLASS_TO_FIELD_SETTERS_MAP_CACHE.put(upperClassName, map);

    return setter;
  }
}