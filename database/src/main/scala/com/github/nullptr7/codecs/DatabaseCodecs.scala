package com.github.nullptr7
package codecs

import skunk._
import skunk.codec.all._

import models.AddressId
import models.Address

object DatabaseCodecs {

  // val addressIdEncoder: Encoder[AddressId] = uuid.values.gcontramap[AddressId]

  // val addressIdDecoder: Decoder[AddressId] = uuid.asDecoder.map(AddressId)

  lazy val addressIdCodec: Codec[AddressId] =
    uuid.imap[AddressId](AddressId)(_.value)

  lazy val dbToAddressDecoder: Decoder[Address] =
    (addressIdCodec.asDecoder ~ text ~ text ~ text ~ text).map { case id ~ street ~ city ~ state ~ zip =>
      Address(id, street, city, state, zip)
    }

  lazy val dbToEmployeeDecoder: Decoder[((((Int, String), Int), Double), Address)] = int4 ~ text ~ int4 ~ float8 ~ dbToAddressDecoder

}
