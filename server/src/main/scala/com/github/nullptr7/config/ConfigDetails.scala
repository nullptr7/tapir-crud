package com.github.nullptr7
package config

object ConfigDetails {

  sealed trait ConfigType {
    val configLocation: Option[String]
    val namespace:      String
  }

  final case object DBProd extends ConfigType {
    val configLocation: Option[String] = None
    val namespace:      String         = "db"
  }

  final case object DBTest extends ConfigType {

    val configLocation: Option[String] = None
    val namespace:      String         = "db"
  }

}
