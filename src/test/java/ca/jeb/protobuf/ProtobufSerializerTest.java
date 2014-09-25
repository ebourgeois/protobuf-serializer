// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.junit.Test;

/**
 * Test class for the Protobuf Serializer.
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public class ProtobufSerializerTest
{
  private static final ProtobufSerializer SERIALIZER = new ProtobufSerializer();

  /**
   * Test method for {@link ca.jeb.protobuf.ProtobufSerializer#toProtoBuf(java.lang.Object)}.
   */
  @Test
  public void testToProtoBuf()
  {

    try
    {
      // 1. Setup the a Pojo Address
      final ca.jeb.protobuf.datamodel.Address address = new ca.jeb.protobuf.datamodel.Address();
      address.setStreet("1 Main St");
      address.setCity("Foo Ville");
      address.setStateOrProvince("Bar");
      address.setPostalCode("J0J 1J1");
      address.setCountry("Canada");

      final ca.jeb.generated.proto.Message.Address protobufAddress = (ca.jeb.generated.proto.Message.Address)SERIALIZER.toProtobuf(address);
      // System.out.println("protobufAddress: " + protobufAddress);

      Assert.assertEquals("Attribute street not equal", address.getStreet(), protobufAddress.getStreet());
      Assert.assertEquals("Attribute city not equal", address.getCity(), protobufAddress.getCity());
      Assert.assertEquals("Attribute stateOrProvince not equal", address.getStateOrProvince(), protobufAddress.getStateOrProvince());
      Assert.assertEquals("Attribute postalCode not equal", address.getPostalCode(), protobufAddress.getPostalCode());
      Assert.assertEquals("Attribute country not equal", address.getCountry(), protobufAddress.getCountry());

      // 2. Setup the a Pojo Person
      final ca.jeb.protobuf.datamodel.Person person = new ca.jeb.protobuf.datamodel.Person();
      person.setName("Erick");
      person.setAge(22);
      person.setAddress(address);

      final ca.jeb.generated.proto.Message.Person protobufPerson = (ca.jeb.generated.proto.Message.Person)SERIALIZER.toProtobuf(person);
      // System.out.println("protobufPerson: " + protobufPerson);

      Assert.assertEquals("Attribute age not equal", (int)person.getAge(), protobufPerson.getAge());
      Assert.assertEquals("Attribute age not equal", person.getName(), protobufPerson.getName());
    }
    catch (Exception e)
    {
      fail("Can not serialize POJO to Protobuf: " + e);
    }

  }

  /**
   * Test method for {@link ca.jeb.protobuf.ProtobufSerializer#fromProtoBuf(com.google.protobuf.GeneratedMessage)}.
   */
  @Test
  public void testFromProtoBuf()
  {
    try
    {
      final ca.jeb.generated.proto.Message.Address protobufAddress = ca.jeb.generated.proto.Message.Address.newBuilder()
              .setStreet("1 Main St").setCity("Foo Ville").setStateOrProvince("Bar").setPostalCode("J0J 1J1").setCountry("Canada").build();

      final ca.jeb.protobuf.datamodel.Address address = (ca.jeb.protobuf.datamodel.Address)SERIALIZER.fromProtobuf(protobufAddress,
              ca.jeb.protobuf.datamodel.Address.class);

      Assert.assertEquals("Attribute street not equal", address.getStreet(), protobufAddress.getStreet());
      Assert.assertEquals("Attribute city not equal", address.getCity(), protobufAddress.getCity());
      Assert.assertEquals("Attribute stateOrProvince not equal", address.getStateOrProvince(), protobufAddress.getStateOrProvince());
      Assert.assertEquals("Attribute postalCode not equal", address.getPostalCode(), protobufAddress.getPostalCode());
      Assert.assertEquals("Attribute country not equal", address.getCountry(), protobufAddress.getCountry());

      Assert.assertFalse("Attribute stret is the same", address.getStreet().isEmpty());

      // 2. Setup the a Pojo Person
      final ca.jeb.generated.proto.Message.Person protobufPerson = ca.jeb.generated.proto.Message.Person.newBuilder().setName("Erick")
              .setAge(22).setAddress(protobufAddress).build();

      final ca.jeb.protobuf.datamodel.Person person = (ca.jeb.protobuf.datamodel.Person)SERIALIZER.fromProtobuf(protobufPerson,
              ca.jeb.protobuf.datamodel.Person.class);
      // System.out.println("person: " + person);

      Assert.assertEquals("Attribute age not equal", (int)person.getAge(), protobufPerson.getAge());
      Assert.assertEquals("Attribute age not equal", person.getName(), protobufPerson.getName());
    }
    catch (Exception e)
    {
      fail("Can not deserialize Protobuf to POJO: " + e);
    }
  }
}
