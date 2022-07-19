package com.github.nullptr7
package codecs

import skunk._
import skunk.codec.all._

import models.Address

object DatabaseCodecs {

  val dbToAddressDecoder: Decoder[Address] = (int4 ~ text ~ text ~ text ~ text).map { case id ~ street ~ city ~ state ~ zip =>
    Address(id, street, city, state, zip)
  }

  val dbToEmployeeDecoder: Decoder[((((Int, String), Int), Double), Address)] = int4 ~ text ~ int4 ~ float8 ~ dbToAddressDecoder

}
