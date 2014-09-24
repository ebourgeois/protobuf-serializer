// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.protobuf;

import static org.junit.Assert.fail;

import org.junit.Test;

import ca.jeb.protobuf.datamodel.Address;

/**
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public class ProtobufSerializerPerfTest
{
  /**
   * Test method for {@link ca.jeb.protobuf.ProtobufSerializer#toProtoBuf(java.lang.Object)}.
   */
  @Test
  public void perfTestToProtoBuf()
  {
    try
    {
      long start = System.currentTimeMillis();
      for (int i = 0; i < 1_000_000; i++)
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
      }
      long end = System.currentTimeMillis();
      System.out.println("Diff: " + (end - start));
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
