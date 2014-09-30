// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Test class for the Protobuf Serializer.
 * 
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProtobufSerializerFromProtobufTest
{
  private ca.jeb.protobuf.datamodel.Address      address;
  private ca.jeb.protobuf.datamodel.Person       person;

  private ca.jeb.generated.proto.Message.Address protobufAddress;
  private ca.jeb.generated.proto.Message.Person  protobufPerson;

  private static final ProtobufSerializer        SERIALIZER = new ProtobufSerializer();

  @Before
  public void setupObjects()
  {
    // Setup Pojo Address
    address = new ca.jeb.protobuf.datamodel.Address();
    address.setStreet("1 Main St");
    address.setCity("Foo Ville");
    address.setStateOrProvince("Bar");
    address.setPostalCode("J0J 1J1");
    address.setCountry("Canada");
    address.setIsCanada("true");
    // Setup POJO Person
    person = new ca.jeb.protobuf.datamodel.Person();
    person.setName("Erick");
    person.setAge(22);
    person.setAddress(address);

    // Setup Address Protobuf
    protobufAddress = ca.jeb.generated.proto.Message.Address.newBuilder().setStreet("1 Main St").setCity("Foo Ville")
            .setStateOrProvince("Bar").setPostalCode("J0J 1J1").setCountry("Canada").setIsCanada(true).build();
    // Setup Person Protobuf
    protobufPerson = ca.jeb.generated.proto.Message.Person.newBuilder().setName("Erick").setAge(22).setAddress(protobufAddress).build();
  }

  @Test
  public void test1Address() throws ProtobufException
  {
    final ca.jeb.protobuf.datamodel.Address address = (ca.jeb.protobuf.datamodel.Address)SERIALIZER.fromProtobuf(protobufAddress,
            ca.jeb.protobuf.datamodel.Address.class);

    Assert.assertEquals("Attribute street not equal", address.getStreet(), protobufAddress.getStreet());
  }

  @Test
  public void test2Address() throws ProtobufException
  {
    final ca.jeb.protobuf.datamodel.Address address = (ca.jeb.protobuf.datamodel.Address)SERIALIZER.fromProtobuf(protobufAddress,
            ca.jeb.protobuf.datamodel.Address.class);

    Assert.assertEquals("Attribute city not equal", address.getCity(), protobufAddress.getCity());
  }

  @Test
  public void test3Address() throws ProtobufException
  {
    final ca.jeb.protobuf.datamodel.Address address = (ca.jeb.protobuf.datamodel.Address)SERIALIZER.fromProtobuf(protobufAddress,
            ca.jeb.protobuf.datamodel.Address.class);

    Assert.assertEquals("Attribute stateOrProvince not equal", address.getStateOrProvince(), protobufAddress.getStateOrProvince());
  }

  @Test
  public void test4Address() throws ProtobufException
  {
    final ca.jeb.protobuf.datamodel.Address address = (ca.jeb.protobuf.datamodel.Address)SERIALIZER.fromProtobuf(protobufAddress,
            ca.jeb.protobuf.datamodel.Address.class);

    Assert.assertEquals("Attribute postalCode not equal", address.getPostalCode(), protobufAddress.getPostalCode());
  }

  @Test
  public void test5Address() throws ProtobufException
  {
    final ca.jeb.protobuf.datamodel.Address address = (ca.jeb.protobuf.datamodel.Address)SERIALIZER.fromProtobuf(protobufAddress,
            ca.jeb.protobuf.datamodel.Address.class);

    Assert.assertEquals("Attribute country not equal", address.getCountry(), protobufAddress.getCountry());
  }

  @Test
  public void test6Address() throws ProtobufException
  {
    final ca.jeb.protobuf.datamodel.Address address = (ca.jeb.protobuf.datamodel.Address)SERIALIZER.fromProtobuf(protobufAddress,
            ca.jeb.protobuf.datamodel.Address.class);

    Assert.assertFalse("Attribute street isEmpty", address.getStreet().isEmpty());
  }

  @Test
  public void test7Person() throws ProtobufException
  {
    final ca.jeb.protobuf.datamodel.Person person = (ca.jeb.protobuf.datamodel.Person)SERIALIZER.fromProtobuf(protobufPerson,
            ca.jeb.protobuf.datamodel.Person.class);
    Assert.assertEquals("Attribute age not equal", (int)person.getAge(), protobufPerson.getAge());
  }

  @Test
  public void test8Person() throws ProtobufException
  {
    final ca.jeb.protobuf.datamodel.Person person = (ca.jeb.protobuf.datamodel.Person)SERIALIZER.fromProtobuf(protobufPerson,
            ca.jeb.protobuf.datamodel.Person.class);
    Assert.assertEquals("Attribute age not equal", person.getName(), protobufPerson.getName());
  }
}