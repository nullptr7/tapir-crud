package com.github.nullptr7
package configurations

package types {

  final case class Sensitive(value: String) extends AnyVal {
    override def toString: String = "MASKED"
  }

  final case class Hostname(value: String) extends AnyVal
  final case class Port(value: Int) extends AnyVal

  final case class Namespace(value: String) extends AnyVal

  trait ConfigType {
    val namespace: Namespace
  }

  object ConfigType {

    implicit object Blaze    extends ConfigType {
      val namespace: Namespace = Namespace("server")
    }

    implicit object Postgres extends ConfigType {
      val namespace: Namespace = Namespace("db")
    }

  }

  final case class ServerConfig(host: Hostname, port: Port)

  final case class DatabaseConfig(
    val host:     Hostname,
    val port:     Port,
    val user:     String,
    val database: String,
    val password: Sensitive
  )

}
