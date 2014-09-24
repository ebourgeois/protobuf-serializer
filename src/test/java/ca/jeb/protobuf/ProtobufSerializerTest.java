// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.junit.Test;

import ca.jeb.protobuf.datamodel.Address;
import ca.jeb.protobuf.datamodel.Person;

/**
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public class ProtobufSerializerTest
{
  /**
   * Test method for {@link ca.jeb.protobuf.ProtobufSerializer#toProtoBuf(java.lang.Object)}.
   */
  @Test
  public void testToProtoBuf()
  {
    try
    {
      // 1. Setup the a Pojo Address
      final Address address = new Address();
      address.setStreet("1 Main St");
      address.setCity("Foo Ville");
      address.setStateOrProvince("Bar");
      address.setPostalCode("J0J 1J1");
      address.setCountry("Canada");

      final ProtobufSerializer<ca.jeb.generated.proto.Message.Address, Address> serializer = new ProtobufSerializer<>();
      final ca.jeb.generated.proto.Message.Address protoBufAddress = serializer.toProtoBuf(address);

      Assert.assertEquals("Attribute street not equal", address.getStreet(), protoBufAddress.getStreet());
      Assert.assertEquals("Attribute city not equal", address.getCity(), protoBufAddress.getCity());
      Assert.assertEquals("Attribute stateOrProvince not equal", address.getStateOrProvince(), protoBufAddress.getStateOrProvince());
      Assert.assertEquals("Attribute postalCode not equal", address.getPostalCode(), protoBufAddress.getPostalCode());
      Assert.assertEquals("Attribute country not equal", address.getCountry(), protoBufAddress.getCountry());

      // 2. Setup the a Pojo Person
      final Person person = new Person();
      person.setName("Erick");
      person.setAge(22);
      person.setAddress(address);

      final ProtobufSerializer<ca.jeb.generated.proto.Message.Person, Person> pSerializer = new ProtobufSerializer<>();
      final ca.jeb.generated.proto.Message.Person protoBufPerson = pSerializer.toProtoBuf(person);

      Assert.assertEquals("Attribute age not equal", (int)person.getAge(), protoBufPerson.getAge());
      Assert.assertEquals("Attribute age not equal", person.getName(), protoBufPerson.getName());
    }
    catch (Exception e)
    {
      fail("Can not serialize Address to protoBuf: " + e);
    }

  }

  /**
   * Test method for {@link ca.jeb.protobuf.ProtobufSerializer#fromProtoBuf(com.google.protobuf.GeneratedMessage)}.
   */
  @Test
  public void testFromProtoBuf()
  {
    // fail("Not yet implemented");
  }

}
