package com.github.nullptr7
package configurations

package types {

  final case class Sensitive(value: String) extends AnyVal {
    override def toString: String = "MASKED"
  }

  final case class Hostname(value: String) extends AnyVal
  final case class Port(value: Int) extends AnyVal

  final case class Namespace(value: String) extends AnyVal
  final case class ConfigLocation(value: String) extends AnyVal

  sealed trait ConfigType {
    val namespace: Namespace
    val location:  Option[ConfigLocation]
  }

  final object DbDev extends ConfigType {
    val namespace: Namespace = Namespace("db")

    val location: Option[ConfigLocation] = None
  }

  final object ServerDev extends ConfigType {
    val location: Option[ConfigLocation] = None

    val namespace: Namespace = Namespace("server")
  }

}
